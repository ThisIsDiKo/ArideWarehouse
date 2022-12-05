package ru.dikoresearch.aridewarehouse.domain.repository.requests

sealed class RequestResult<out L>{
    data class Success<out L>(val value: L): RequestResult<L>()
    data class HttpError(val code: Int, val errorCause: String): RequestResult<Nothing>()
    data class UnknownError(val errorCause: String): RequestResult<Nothing>()
    object Unauthorized: RequestResult<Nothing>()
}
