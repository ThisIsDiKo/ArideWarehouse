package ru.dikoresearch.aridewarehouse.presentation.orderdetails

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.dikoresearch.aridewarehouse.domain.entities.Order
import ru.dikoresearch.aridewarehouse.domain.entities.OrderImage
import ru.dikoresearch.aridewarehouse.domain.repository.requests.RequestResult
import ru.dikoresearch.aridewarehouse.domain.repository.WarehouseRepository
import ru.dikoresearch.aridewarehouse.domain.workers.ImageUploadWorker
import ru.dikoresearch.aridewarehouse.presentation.utils.BASE_URL_IMAGES
import ru.dikoresearch.aridewarehouse.presentation.utils.NavigationConstants
import ru.dikoresearch.aridewarehouse.presentation.utils.NavigationEvent
import ru.dikoresearch.aridewarehouse.presentation.utils.getFormattedDateFromDataBaseDate
import javax.inject.Inject
import kotlin.properties.Delegates

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

    private val _listOfGoods = MutableStateFlow(emptyList<OrderGoodsAdapterModel>())
    val listOfGoods: StateFlow<List<OrderGoodsAdapterModel>> = _listOfGoods.asStateFlow()

    private var workInfoLiveData: LiveData<List<WorkInfo>>? = null
    private val workInfoObserver: Observer<List<WorkInfo>> = Observer{ l ->
        if (l.size == 1){
            val workInfo = l.first()
            when(workInfo.state){
                WorkInfo.State.SUCCEEDED -> {
                    _showProgressBar.value = false
                    setAllImagesLoaded()
                }
                WorkInfo.State.FAILED -> {
                    _showProgressBar.value = false
                    val outputResult = workInfo.outputData
                        .getString(ImageUploadWorker.WORKER_OUTPUT_DATA_KEY) ?: "Empty message"

                    viewModelScope.launch {
                        _navigationEvent.send(
                            NavigationEvent.ShowToast(outputResult)
                        )
                    }
                }
                WorkInfo.State.CANCELLED -> {
                    _showProgressBar.value = false
                }
                else -> {

                }
            }

        }

    }

    private var orderFullInfo: Order by Delegates.notNull()

    override fun onCleared() {
        super.onCleared()
        workInfoLiveData?.removeObserver(workInfoObserver)
    }

    fun uploadToServer(comment: String, workManager: WorkManager){
        viewModelScope.launch {
            if (!checkIfThereIsImagesToUpload()){
                return@launch
            }

            _showProgressBar.value = true

            if (_orderDetailsState.value.status == "New"){
                val orderToSend = orderFullInfo.copy(
                    comment = comment,
                    checked = 1
                )

                val result = warehouseRepository.createNewOrder(orderToSend)
                when(result){
                    is RequestResult.Success -> {
                        updateStateViewOrder(result.value)
                        updateListOfGoodsWithOrder(result.value)
                        startUpload(result.value.orderId, workManager)
                    }
                    is RequestResult.Unauthorized -> {
                        _navigationEvent.send(NavigationEvent.Navigate(NavigationConstants.LOGIN_SCREEN))
                        _showProgressBar.value = false
                    }
                    is RequestResult.HttpError -> {
                        _navigationEvent.send(NavigationEvent.ShowToast("Server Error ${result.code}"))
                        _showProgressBar.value = false

                    }
                    is RequestResult.UnknownError -> {
                        _navigationEvent.send(NavigationEvent.ShowToast("Unknown Error ${result.errorCause}"))
                        _showProgressBar.value = false

                    }
                }
            }
            else {
                startUpload(_orderDetailsState.value.orderId, workManager)
            }

        }
    }

    fun getListOfImagesUrls(): Array<String>{
        return listOfImages.value.filter { !it.newImageActionHolder }.map{it.imageUri}.toTypedArray()
    }

    fun loadOrder(orderName: String){
        viewModelScope.launch {
            _showProgressBar.value = true

            val result = warehouseRepository.getOrderByName(orderName)

            when(result){
                is RequestResult.Success -> {
                    updateStateViewOrder(result.value)
                    updateListOfGoodsWithOrder(result.value)
                    updateImagesListWithOrder(result.value)
                }
                is RequestResult.Unauthorized -> {
                    _navigationEvent.send(NavigationEvent.Navigate(NavigationConstants.LOGIN_SCREEN))
                }
                is RequestResult.HttpError -> {
                    _orderDetailsState.value = orderDetailsState.value.copy(
                        orderName = orderName
                    )
                    _navigationEvent.send(NavigationEvent.ShowToast("Server Error ${result.code}"))
                }
                is RequestResult.UnknownError -> {
                    _orderDetailsState.value = orderDetailsState.value.copy(
                        orderName = orderName
                    )
                    _navigationEvent.send(NavigationEvent.ShowToast("Unknown Error ${result.errorCause}"))
                }
            }

            _showProgressBar.value = false
        }
    }

    fun updateComment(text: String){
        _orderDetailsState.value = _orderDetailsState.value.copy(comment = text)
    }

    fun getAllowedNumberOfImages(): Int {
        val maxNumOfImages = 3
        return maxNumOfImages + 1 - _listOfImages.value.size
    }

    fun addImage(image: OrderImage){
        val temp = _listOfImages.value.toMutableList()
        if (temp.size == 3){
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
        if (temp.size == 2){
            temp.add(OrderImage(imageName = "", loaded = true, newImageActionHolder = true))
        }
        _listOfImages.value = temp.toList()

        _orderDetailsState.value = _orderDetailsState.value.copy(hasImagesToUpload = checkIfThereIsImagesToUpload())
    }

    fun setCheckedStateToGoods(index: Int, state: Boolean){
        val temp = _listOfGoods.value.toMutableList()
        temp[index] = temp[index].copy(isChecked = state)

        _listOfGoods.value = temp.toList()

        val isAllChecked = temp.all { it.isChecked }

        _orderDetailsState.value = orderDetailsState.value.copy(allGoodsChecked = isAllChecked)
    }

    private fun updateStateViewOrder(order: Order){
        val date = try {
            order.createdAt.getFormattedDateFromDataBaseDate()
        }
        catch (e: Exception){
            ""
        }

        orderFullInfo = order.copy()

        _orderDetailsState.value = OrderDetailsState(
            orderId = order.orderId,
            orderName = order.orderName,
            username = order.username,
            status = order.status,
            createdAt = date,
            comment = order.comment,
            allGoodsChecked = order.checked > 0
        )
    }

    private fun updateListOfGoodsWithOrder(order: Order){
        _listOfGoods.value = order.goods.map {
            OrderGoodsAdapterModel(
                goods = it,
                isChecked = order.checked > 0,
                isLoaded = order.checked > 0
            )
        }
    }

    private fun updateImagesListWithOrder(order: Order){
        order.images.forEach {
            val image = OrderImage(
                imageName = it.split("/").last(),
                loaded = true,
                imageUri = "$BASE_URL_IMAGES$it",
                newImageActionHolder = false
            )
            addImage(image)
        }
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

        workManager.enqueueUniqueWork(
            "UniqueImageUploadWorker",
            ExistingWorkPolicy.KEEP,
            worker
        )

        workInfoLiveData = workManager.getWorkInfosForUniqueWorkLiveData("UniqueImageUploadWorker")
        workInfoLiveData?.observeForever(
            workInfoObserver
        )


    }


}