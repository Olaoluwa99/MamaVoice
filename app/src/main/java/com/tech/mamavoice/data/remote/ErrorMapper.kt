package com.tech.mamavoice.data.remote

import com.tech.mamavoice.domain.util.AppError
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Maps a raw throwable from the network layer to a typed [AppError] the UI can localize.
 *
 * For validation-type responses (400/409/422) we surface the backend's own `message` when it's
 * present and reasonable, since it's specific and safe; every other status falls back to our own
 * friendly category so the user never sees a raw "HTTP 401".
 */
fun Throwable.toAppError(): AppError = when (this) {
    is HttpException -> when (val code = code()) {
        400, 409, 422 -> serverMessage()?.let { AppError.Message(it) } ?: AppError.Validation
        401 -> AppError.Unauthorized
        403 -> AppError.Forbidden
        404 -> AppError.NotFound
        408 -> AppError.Timeout
        in 500..599 -> AppError.Server
        else -> AppError.Unknown
    }
    is SocketTimeoutException -> AppError.Timeout
    is UnknownHostException -> AppError.Network
    is IOException -> AppError.Network
    else -> AppError.Unknown
}

@Serializable
private data class ServerErrorBody(val message: String? = null)

private val lenientJson = Json { ignoreUnknownKeys = true }

/**
 * Pulls the `message` out of an [ApiResponse]-shaped error body, if it's short enough to be a
 * user-facing sentence rather than a stack trace or technical dump.
 */
private fun HttpException.serverMessage(): String? {
    val raw = response()?.errorBody()?.string()?.takeIf { it.isNotBlank() } ?: return null
    return runCatching {
        lenientJson.decodeFromString(ServerErrorBody.serializer(), raw).message
    }.getOrNull()
        ?.trim()
        ?.takeIf { it.isNotBlank() && it.length <= 160 }
}
