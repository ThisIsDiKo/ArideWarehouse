package ru.dikoresearch.aridewarehouse.domain.repository.responses

import ru.dikoresearch.aridewarehouse.domain.entities.Order

data class ListOfOrdersResponse(
    val orders: List<Order>
)
