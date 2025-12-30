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
}
