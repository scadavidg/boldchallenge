package com.domain.result

import com.domain.error.AppError

/**
 * Sealed class representing the state of an asynchronous operation.
 * Provides explicit state management for UI updates.
 *
 * @param T The type of data contained in Success state
 */
sealed class ResultState<out T> {
    /**
     * Operation is in progress.
     * Optionally contains cached data to display while loading.
     */
    data class Loading<T>(val data: T? = null) : ResultState<T>()

    /**
     * Operation completed successfully.
     */
    data class Success<T>(val data: T) : ResultState<T>()

    /**
     * Operation failed with an error.
     */
    data class Failure(val error: AppError) : ResultState<Nothing>()

    /**
     * Returns the data if state is Success or Loading with data, null otherwise.
     */
    fun dataOrNull(): T? = when (this) {
        is Loading -> data
        is Success -> data
        is Failure -> null
    }

    /**
     * Returns the error if state is Failure, null otherwise.
     */
    fun errorOrNull(): AppError? = when (this) {
        is Loading -> null
        is Success -> null
        is Failure -> error
    }

    /**
     * Returns true if state is Loading.
     */
    fun isLoading(): Boolean = this is Loading

    /**
     * Returns true if state is Success.
     */
    fun isSuccess(): Boolean = this is Success

    /**
     * Returns true if state is Failure.
     */
    fun isFailure(): Boolean = this is Failure
}

/**
 * Maps the data type of ResultState using the provided transform function.
 */
fun <T, R> ResultState<T>.map(transform: (T) -> R): ResultState<R> = when (this) {
    is ResultState.Loading -> ResultState.Loading(data?.let(transform))
    is ResultState.Success -> ResultState.Success(transform(data))
    is ResultState.Failure -> this
}

/**
 * Maps the error of ResultState using the provided transform function.
 */
fun <T> ResultState<T>.mapError(transform: (AppError) -> AppError): ResultState<T> = when (this) {
    is ResultState.Loading -> this
    is ResultState.Success -> this
    is ResultState.Failure -> ResultState.Failure(transform(error))
}

