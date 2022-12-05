package ru.dikoresearch.aridewarehouse.presentation.orderslist

import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.dikoresearch.aridewarehouse.domain.entities.OrdersListItem
import ru.dikoresearch.aridewarehouse.domain.repository.requests.RequestResult
import ru.dikoresearch.aridewarehouse.domain.repository.WarehouseRepository
import ru.dikoresearch.aridewarehouse.presentation.utils.NavigationConstants
import ru.dikoresearch.aridewarehouse.presentation.utils.NavigationEvent
import ru.dikoresearch.aridewarehouse.presentation.utils.ORDER_NAME
import java.io.File
import java.time.LocalDateTime
import javax.inject.Inject

class OrdersListViewModel @Inject constructor(
    private val warehouseRepository: WarehouseRepository
): ViewModel() {

    private val _listOfOrders = MutableStateFlow(listOf<OrdersListItem>())
    val listOfOrders: StateFlow<List<OrdersListItem>> = _listOfOrders.asStateFlow()

    private val _showProgressBar = MutableStateFlow(false)
    val showProgressBar: StateFlow<Boolean> = _showProgressBar.asStateFlow()

    private val _navigationEvent = Channel<NavigationEvent>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private var loadedListOfOrders = listOf<OrdersListItem>()


    fun clearFiles(){
           viewModelScope.launch {
               _showProgressBar.value = true

               //TODO think about providing context
               val directoryWithPictures = File("sdcard/Android/data/ru.dikoresearch.aridewarehouse/files/Pictures")
               val result = directoryWithPictures.deleteRecursively()

               _navigationEvent.send(
                   NavigationEvent.ShowToast(if(result) "Изображения успешно удалены" else "Ошибка при удалении изображений")
               )

               _showProgressBar.value = false
           }
    }

    fun loadOrders(){
        viewModelScope.launch {
            _showProgressBar.value = true
            val result = warehouseRepository.getAllOrders()
            _showProgressBar.value = false

            when(result){
                is RequestResult.Success -> {
                    loadedListOfOrders = result.value.orders.map { orderInfo ->
                        OrdersListItem(
                            orderId = orderInfo.orderId,
                            orderName = orderInfo.orderName,
                            status = orderInfo.status,
                            username = orderInfo.username,
                            createdAt = orderInfo.createdAt
                        )
                    }
                    _listOfOrders.value = loadedListOfOrders
                }
                is RequestResult.Unauthorized -> {
                    _navigationEvent.send(NavigationEvent.Navigate(NavigationConstants.LOGIN_SCREEN))
                }
                is RequestResult.HttpError -> {
                    _navigationEvent.send(NavigationEvent.ShowToast("Server Error ${result.code} ${result.errorCause}"))
                }
                is RequestResult.UnknownError -> {
                    _navigationEvent.send(NavigationEvent.ShowToast("Unknown Error ${result.errorCause}"))
                }
            }
        }
    }

    fun logout(){
        viewModelScope.launch {
            warehouseRepository.logout()
            _navigationEvent.send(NavigationEvent.Navigate(NavigationConstants.LOGIN_SCREEN))
        }
    }

    fun showOrderDetails(orderName: String){
        viewModelScope.launch {
            _navigationEvent.send(
                NavigationEvent.Navigate(NavigationConstants.DETAILS_SCREEN, bundleOf(ORDER_NAME to orderName))
            )
        }
    }

    fun search(query: String){
        if (query.isNotBlank()){
            val result = loadedListOfOrders.filter {
                it.orderName.contains(query) || it.username.contains(query)
            }
            _listOfOrders.value = result
        }
        else {
            _listOfOrders.value = loadedListOfOrders
        }
    }

    fun sortByName(){
        _listOfOrders.value = loadedListOfOrders.sortedBy { it.orderName }
    }

    fun sortByDate(){
        _listOfOrders.value = loadedListOfOrders.sortedByDescending {
            LocalDateTime.parse(it.createdAt)
        }
    }
}