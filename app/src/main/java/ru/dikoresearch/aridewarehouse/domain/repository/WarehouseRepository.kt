package ru.dikoresearch.aridewarehouse.domain.repository

import android.graphics.Bitmap
import ru.dikoresearch.aridewarehouse.domain.entities.OrderFullInfo
import ru.dikoresearch.aridewarehouse.domain.entities.OrderInfo
import ru.dikoresearch.aridewarehouse.domain.repository.requests.RequestResult
import ru.dikoresearch.aridewarehouse.domain.repository.responses.ListOfOrdersResponse
import ru.dikoresearch.aridewarehouse.domain.repository.responses.LoginResponse

interface WarehouseRepository {
    suspend fun login(username: String, password: String): RequestResult<LoginResponse>
    suspend fun logout()

    suspend fun getAllOrders(): RequestResult<ListOfOrdersResponse>

    suspend fun getOrderByName(orderName: String): RequestResult<OrderFullInfo>

    suspend fun createNewOrder(orderFullInfo: OrderFullInfo): RequestResult<OrderFullInfo>


    suspend fun uploadImage(orderId: Int, imageName: String, image: Bitmap): RequestResult<Unit>
}