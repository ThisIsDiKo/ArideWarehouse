package ru.dikoresearch.aridewarehouse.domain.repository.responses

import ru.dikoresearch.aridewarehouse.domain.entities.OrderInfo

data class ListOfOrdersResponse(
    val orders: List<OrderInfo>
)
