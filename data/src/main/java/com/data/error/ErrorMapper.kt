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

        // Direct Moshi exceptions
        is com.squareup.moshi.JsonDataException,
        is com.squareup.moshi.JsonEncodingException -> AppError.ParseError(
            message = message ?: "Failed to parse data",
            cause = this
        )

        // IllegalStateException wrapping Moshi exceptions (from mappers)
        // Mappers convert Moshi exceptions to IllegalStateException to hide implementation details
        is IllegalStateException -> {
            val underlyingCause = cause
            when {
                underlyingCause is com.squareup.moshi.JsonDataException ||
                    underlyingCause is com.squareup.moshi.JsonEncodingException ||
                    underlyingCause is java.io.EOFException -> AppError.CacheError(
                    message = message ?: "Failed to deserialize cached data"
                )
                message?.contains("deserialize", ignoreCase = true) == true -> 
                    AppError.CacheError(
                        message = message ?: "Failed to deserialize cached data"
                    )
                else -> AppError.UnknownError(
                    message = message ?: "An unexpected error occurred",
                    cause = this
                )
            }
        }

        is android.database.sqlite.SQLiteException -> AppError.CacheError(
            message = message ?: "Database error occurred"
        )

        else -> AppError.UnknownError(
            message = message ?: "An unexpected error occurred",
            cause = this
        )
    }
}

