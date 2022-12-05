package ru.dikoresearch.aridewarehouse.presentation.login

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.dikoresearch.aridewarehouse.domain.repository.requests.RequestResult
import ru.dikoresearch.aridewarehouse.domain.repository.WarehouseRepository
import ru.dikoresearch.aridewarehouse.presentation.utils.NavigationConstants
import ru.dikoresearch.aridewarehouse.presentation.utils.NavigationEvent
import ru.dikoresearch.aridewarehouse.presentation.utils.WAREHOUSE_TOKEN
import ru.dikoresearch.aridewarehouse.presentation.utils.WAREHOUSE_USERNAME
import javax.inject.Inject

class LogInViewModel @Inject constructor(
    private val warehouseRepository: WarehouseRepository,
    private val sharedPreferences: SharedPreferences
): ViewModel() {
    private val _showProgressBar = MutableStateFlow(false)
    val showProgressBar: StateFlow<Boolean> = _showProgressBar.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    private val _navigationEvent = Channel<NavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    fun login(username: String, password: String){
        viewModelScope.launch {
            _showProgressBar.value = true
            _errorMessage.value = ""

            val result = warehouseRepository.login(username=username, password=password)

            _showProgressBar.value = false

            when(result){
                is RequestResult.Success -> {
                    val response = result.value

                    sharedPreferences.edit()
                        .putString(WAREHOUSE_TOKEN, response.token)
                        .putString(WAREHOUSE_USERNAME, response.username)
                        .apply()


                    _navigationEvent.send(
                        NavigationEvent.Navigate(NavigationConstants.ORDERS_LIST_SCREEN)
                    )

                }
                is RequestResult.Unauthorized -> {

                }
                is RequestResult.HttpError -> {
                    if (result.code == 404){
                        _errorMessage.value = "Неверное имя пользователя или пароль"
                    }
                    else {
                        _navigationEvent.send(
                            NavigationEvent.ShowToast("Server error ${result.code}")
                        )
                    }
                }
                is RequestResult.UnknownError -> {
                    _navigationEvent.send(
                        NavigationEvent.ShowToast("Unknown Error ${result.errorCause}")
                    )
                }
            }
        }
    }
}