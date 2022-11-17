package ru.dikoresearch.aridewarehouse.domain.repository.requests

sealed class RequestResult<out T>{
    data class Success<out T>(val value: T): RequestResult<T>()
    data class Error(val code: Int? = null, val errorCause: String? = null ): RequestResult<Nothing>()
}
