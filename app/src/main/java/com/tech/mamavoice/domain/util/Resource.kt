package com.tech.mamavoice.domain.util

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)

    /**
     * [error] is the typed classification the UI localizes into friendly copy; [message] is kept as
     * a raw/diagnostic fallback for logging and for the rare call site that hasn't adopted [error].
     */
    class Error<T>(
        message: String,
        val error: AppError? = null,
        data: T? = null
    ) : Resource<T>(data, message)

    class Loading<T>(data: T? = null) : Resource<T>(data)
}
