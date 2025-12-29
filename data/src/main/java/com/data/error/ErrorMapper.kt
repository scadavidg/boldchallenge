package com.data.error

import com.domain.error.AppError
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

/**
 * Maps Throwable exceptions to domain AppError.
 * Provides centralized error mapping logic.
 */
fun Throwable.toAppError(): AppError {
    return when (this) {
        is SocketTimeoutException,
        is TimeoutException -> AppError.NetworkError.Timeout

        is UnknownHostException,
        is ConnectException -> AppError.NetworkError.NoConnection

        is HttpException -> AppError.NetworkError.HttpError(
            code = code(),
            message = message()
        )

        is com.squareup.moshi.JsonDataException,
        is com.squareup.moshi.JsonEncodingException -> AppError.ParseError(
            message = message ?: "Failed to parse data",
            cause = this
        )

        is android.database.sqlite.SQLiteException -> AppError.CacheError(
            message = message ?: "Database error occurred"
        )

        else -> AppError.UnknownError(
            message = message ?: "An unexpected error occurred",
            cause = this
        )
    }
}

