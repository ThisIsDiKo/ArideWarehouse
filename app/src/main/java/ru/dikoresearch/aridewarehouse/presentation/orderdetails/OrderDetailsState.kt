package ru.dikoresearch.aridewarehouse.presentation.orderdetails

data class OrderDetailsState(
    val orderId: Int = -1,
    val orderName: String = "Unknown",
    val username: String = "",
    val status: String = "New",
    val createdAt: String = "",
    val comment: String = "",
    val hasImagesToUpload: Boolean = false,
    val allGoodsChecked: Boolean = false
)
