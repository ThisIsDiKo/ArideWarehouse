package ru.dikoresearch.aridewarehouse.data.repository

import android.content.SharedPreferences
import android.graphics.Bitmap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import ru.dikoresearch.aridewarehouse.domain.entities.OrderFullInfo
import ru.dikoresearch.aridewarehouse.domain.entities.OrderInfo
import ru.dikoresearch.aridewarehouse.domain.repository.requests.RequestResult
import ru.dikoresearch.aridewarehouse.domain.repository.WarehouseRepository
import ru.dikoresearch.aridewarehouse.domain.repository.requests.LoginRequest
import ru.dikoresearch.aridewarehouse.domain.repository.requests.OrderCreateRequest
import ru.dikoresearch.aridewarehouse.domain.repository.responses.ListOfOrdersResponse
import ru.dikoresearch.aridewarehouse.presentation.utils.setToken
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class WarehouseRepositoryImpl @Inject constructor(
    // var 1. @param:Named("prod")
    //@Prod //вар 2 через свою аннотацию
    private val warehouseService: WarehouseService,
    private val sharedPreferences: SharedPreferences,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
): WarehouseRepository {

    override suspend fun login(username: String, password: String) = safeCallApi(dispatcher){
        warehouseService.login(LoginRequest(username = username, password = password))
    }

    override suspend fun logout() {
        //Maybe we need to send information to server
        sharedPreferences.setToken(token = "", username = "")
    }

    override suspend fun getAllOrders(): RequestResult<ListOfOrdersResponse> {
        return safeCallApi(dispatcher){
            warehouseService.getAllOrders()
        }
    }

    override suspend fun getOrderByName(orderName: String): RequestResult<OrderFullInfo> {
        return safeCallApi(dispatcher){
            warehouseService.getOrderByName(orderName)
        }
    }

    override suspend fun createNewOrder(
        orderFullInfo: OrderFullInfo
    ): RequestResult<OrderFullInfo> {
        return safeCallApi(dispatcher){
            warehouseService.createNewOrder(orderFullInfo)
        }
    }

    override suspend fun uploadImage(
        orderId: Int,
        imageName: String,
        image: Bitmap
    ): RequestResult<Unit> {
        return safeCallApi(dispatcher){
            val stream = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 40, stream)
            val byteArray = stream.toByteArray()
            val body = MultipartBody.Part.createFormData(
                "photo[content]",
                imageName,
                byteArray.toRequestBody("image/*".toMediaTypeOrNull(), 0, byteArray.size)
            )
            warehouseService.uploadImage(
                orderId = orderId.toString(),
                imageName = imageName,
                body = body
            )
        }
    }


    private suspend fun <T> safeCallApi(dispatcher: CoroutineDispatcher, call: suspend() -> T): RequestResult<T> {
        return withContext(dispatcher){
            try{
                RequestResult.Success(call.invoke())
            }
            catch(e: HttpException){
                RequestResult.Error(code = e.code(), errorCause = e.message)
            }
            catch (e: Exception){
                RequestResult.Error(code = null, errorCause = e.message)
            }
        }
    }
}