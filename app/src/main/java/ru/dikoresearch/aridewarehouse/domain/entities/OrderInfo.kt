package ru.dikoresearch.aridewarehouse.domain.entities

data class OrderInfo(
    val orderId: Int,
    val orderName: String,
    val username: String,
    val status: String,
    val createdAt: String,
    val comment: String,
    val images: List<String>
)
