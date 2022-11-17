package ru.dikoresearch.aridewarehouse.data.repository

import okhttp3.MultipartBody
import retrofit2.http.*
import ru.dikoresearch.aridewarehouse.domain.entities.OrderInfo
import ru.dikoresearch.aridewarehouse.domain.repository.requests.LoginRequest
import ru.dikoresearch.aridewarehouse.domain.repository.requests.OrderCreateRequest
import ru.dikoresearch.aridewarehouse.domain.repository.responses.ListOfOrdersResponse
import ru.dikoresearch.aridewarehouse.domain.repository.responses.LoginResponse

interface WarehouseService {

    @POST("user/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @GET("orders/all")
    suspend fun getAllOrders(): ListOfOrdersResponse

    @GET("orders/{id}")
    suspend fun getOrderById(@Path("id") orderId: Int): OrderInfo

    @GET("orders/name/{name}")
    suspend fun getOrderByName(@Path("name") orderName: String): OrderInfo

    @POST("orders/new")
    suspend fun createNewOrder(@Body newOrder: OrderCreateRequest): OrderInfo

    @POST("orders/uploadImage")
    @Multipart
    suspend fun uploadImage(
        @Part("orderId") orderId: String,
        @Part("imageName") imageName: String,
        @Part body: MultipartBody.Part
    )
}