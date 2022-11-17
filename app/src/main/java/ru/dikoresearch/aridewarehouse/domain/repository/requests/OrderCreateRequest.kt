package ru.dikoresearch.aridewarehouse.domain.repository.requests

data class OrderCreateRequest(
    val orderName: String,
    val comment: String
)
