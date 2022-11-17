package ru.dikoresearch.aridewarehouse.domain.repository.responses

data class LoginResponse(
    val username: String,
    val token: String
)