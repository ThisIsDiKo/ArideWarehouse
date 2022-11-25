package ru.dikoresearch.aridewarehouse.domain.repository.responses

data class OrderFullInfoResponse(
    val orderId: Int,
    val orderName: String,
    val username: String,
    val status: String,
    val createdAt: String,
    val comment: String,
    val images: List<String>,
    val uuid: String,
    val goods: String,
    val checked: Int
)
