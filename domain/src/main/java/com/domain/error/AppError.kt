package com.domain.error

/**
 * Sealed class representing application errors.
 * Provides type-safe error handling with specific error categories.
 */
sealed class AppError {
    /**
     * Network-related errors
     */
    sealed class NetworkError : AppError() {
        /**
         * Request timed out
         */
        object Timeout : NetworkError()

        /**
         * No internet connection available
         */
        object NoConnection : NetworkError()

        /**
         * HTTP error with status code and message
         */
        data class HttpError(
            val code: Int,
            val message: String
        ) : NetworkError()
    }

    /**
     * Cache/database-related errors
     */
    data class CacheError(
        val message: String
    ) : AppError()

    /**
     * Data parsing/serialization errors
     */
    data class ParseError(
        val message: String,
        val cause: Throwable? = null
    ) : AppError()

    /**
     * Unknown/unexpected errors
     */
    data class UnknownError(
        val message: String,
        val cause: Throwable? = null
    ) : AppError()
}

