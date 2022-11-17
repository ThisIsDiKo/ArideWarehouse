package ru.dikoresearch.aridewarehouse.presentation.orderdetails

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.dikoresearch.aridewarehouse.domain.entities.OrderImage
import ru.dikoresearch.aridewarehouse.domain.repository.requests.RequestResult
import ru.dikoresearch.aridewarehouse.domain.repository.WarehouseRepository
import ru.dikoresearch.aridewarehouse.domain.workers.ImageUploadWorker
import ru.dikoresearch.aridewarehouse.presentation.utils.NavigationEvent
import ru.dikoresearch.aridewarehouse.presentation.utils.getFormattedDateFromDataBaseDate
import javax.inject.Inject

class OrderDetailsViewModel @Inject constructor(
    private val warehouseRepository: WarehouseRepository
): ViewModel() {

    private val _orderDetailsState = MutableStateFlow(OrderDetailsState())
    val orderDetailsState: StateFlow<OrderDetailsState> = _orderDetailsState.asStateFlow()

    private val _showProgressBar = MutableStateFlow(false)
    val showProgressBar: StateFlow<Boolean> = _showProgressBar.asStateFlow()

    private val _navigationEvent = Channel<NavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private val _listOfImages = MutableStateFlow(listOf(OrderImage(imageName = "", newImageActionHolder = true)))
    val listOfImages: StateFlow<List<OrderImage>> = _listOfImages.asStateFlow()

    fun uploadToServer(orderName: String, comment: String, workManager: WorkManager){
        viewModelScope.launch {
            if (!checkIfThereIsImagesToUpload()){
                return@launch
            }

            _showProgressBar.value = true

            if (_orderDetailsState.value.status == "New"){
                val result = warehouseRepository.createNewOrder(orderName, comment)
                when(result){
                    is RequestResult.Success -> {
                        val date = result.value.createdAt.getFormattedDateFromDataBaseDate()
                        _orderDetailsState.value = OrderDetailsState(
                            orderId = result.value.orderId,
                            orderName = result.value.orderName,
                            username = result.value.username,
                            status = result.value.status,
                            createdAt = date,
                            comment = result.value.comment
                        )

                        startUpload(result.value.orderId, workManager)
                    }
                    is RequestResult.Error -> {
                        Log.e("Order Details Vew Model", "Unexpected error: ${result.code} -> ${result.errorCause}")
                        if (result.code == 401){
                            _navigationEvent.send(NavigationEvent.Navigate("Login"))
                        }
                        else if (result.code != null) {
                            _navigationEvent.send(NavigationEvent.ShowToast("Server Error ${result.code}"))
                        }
                        else {
                            _navigationEvent.send(NavigationEvent.ShowToast("Unknown Error ${result.errorCause}"))
                        }
                        _showProgressBar.value = false
                    }
                }
            }
            else {
                startUpload(_orderDetailsState.value.orderId, workManager)
            }

        }
    }

    fun loadOrder(orderName: String){
        viewModelScope.launch {
            _showProgressBar.value = true

            val result = warehouseRepository.getOrderByName(orderName)

            when(result){
                is RequestResult.Success -> {
                    val date = result.value.createdAt.getFormattedDateFromDataBaseDate()
                    _orderDetailsState.value = OrderDetailsState(
                        orderId = result.value.orderId,
                        orderName = result.value.orderName,
                        username = result.value.username,
                        status = result.value.status,
                        createdAt = date,
                        comment = result.value.comment
                    )

                    result.value.images.forEach {
                        val image = OrderImage(
                            imageName = it.split("/").last(),
                            loaded = true,
                            imageUri = "http://172.16.1.54:8080/orders/image/$it",
                            newImageActionHolder = false
                        )
                        addImage(image)
                    }
                }
                is RequestResult.Error -> {
                    Log.e("Order Details Vew Model", "Unexpected error: ${result.code} -> ${result.errorCause}")
                    if (result.code == 401){
                        _navigationEvent.send(NavigationEvent.Navigate("Login"))
                    }
                    else if (result.code != null) {
                        _navigationEvent.send(NavigationEvent.ShowToast("Server Error ${result.code}"))
                    }
                    else {
                        _navigationEvent.send(NavigationEvent.ShowToast("Unknown Error ${result.errorCause}"))
                    }
                    _orderDetailsState.value = orderDetailsState.value.copy(
                        orderName = orderName
                    )
                }
            }

            _showProgressBar.value = false
        }
    }

    fun addImage(image: OrderImage){
        val temp = _listOfImages.value.toMutableList()
        if (temp.size == 6){
            temp.removeLast()
            temp.add(image)
        }
        else {
            temp.add(temp.size - 1, image)
        }
        _listOfImages.value = temp.toList()

        _orderDetailsState.value = _orderDetailsState.value.copy(hasImagesToUpload = checkIfThereIsImagesToUpload())
    }

    private fun setAllImagesLoaded(){
        val temp = _listOfImages.value.toMutableList()
        _listOfImages.value = temp.map {
            it.copy(loaded = true)
        }
        _orderDetailsState.value = _orderDetailsState.value.copy(hasImagesToUpload = checkIfThereIsImagesToUpload())
    }

    fun removeImage(image: OrderImage){
        val temp = _listOfImages.value.toMutableList()
        val index = temp.indexOf(image)
        temp.removeAt(index)
        if (temp.size == 5){
            temp.add(OrderImage(imageName = "", loaded = true, newImageActionHolder = true))
        }
        _listOfImages.value = temp.toList()

        _orderDetailsState.value = _orderDetailsState.value.copy(hasImagesToUpload = checkIfThereIsImagesToUpload())
    }

    private fun checkIfThereIsImagesToUpload(): Boolean {
        val notUploadedImages = _listOfImages.value.filter { !it.loaded && !it.newImageActionHolder}
        if (notUploadedImages.isEmpty()){
            return false
        }
        return true
    }


    private fun startUpload(orderId: Int, workManager: WorkManager){

        val imagesUriArray = _listOfImages.value
            .filter { !it.loaded && !it.newImageActionHolder}
            .map { it.imageUri }
            .toTypedArray()



        val inputData = workDataOf(
            ImageUploadWorker.WORKER_ORDER_ID_KEY to orderId,
            ImageUploadWorker.WORKER_IMAGE_NAME_KEY to "Unknown",
            ImageUploadWorker.WORKER_IMAGES_ARRAY_KEY to imagesUriArray
        )

        Log.d("", "Input data to worker: $inputData")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val worker = OneTimeWorkRequestBuilder<ImageUploadWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        val operation = workManager.enqueueUniqueWork(
            "UniqueImageUploadWorker",
            ExistingWorkPolicy.KEEP,
            worker
        ).result

        operation.addListener(
            {
                if (operation.isDone){
                    Log.e("dd", "Worker Upload completed successfully")
                    setAllImagesLoaded()
                }
                else {
                    Log.e("dd", "Worker Upload failed")
                }
                _showProgressBar.value = false
            },
            {
                it.run()
            }
        )

    }


}