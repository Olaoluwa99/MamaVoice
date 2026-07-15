package com.tech.mamavoice.domain.util

/**
 * Typed, UI-agnostic classification of a failure. Repositories map raw exceptions to one of these
 * (see `Throwable.toAppError()`), and the presentation layer turns it into friendly, localized copy
 * via `appErrorMessage(...)`.
 *
 * Keeping errors typed here (instead of baking English strings into the data layer) matters because
 * the app applies the user's language with a Compose-only locale override — the data layer's
 * `Context` is not locale-aware, so the human-readable text must be resolved in `@Composable` code.
 */
sealed class AppError {
    /** No connection / host unreachable. */
    data object Network : AppError()

    /** Request timed out (slow or unresponsive server). */
    data object Timeout : AppError()

    /** 401 — the session is no longer valid; the user should sign in again. */
    data object Unauthorized : AppError()

    /** 403 — authenticated but not allowed. */
    data object Forbidden : AppError()

    /** 404 — the requested resource doesn't exist. */
    data object NotFound : AppError()

    /** 5xx — the server failed. */
    data object Server : AppError()

    /** 400/409/422 with no usable server text — the input needs attention. */
    data object Validation : AppError()

    /**
     * A specific, safe message supplied by the backend (validation-type responses). Shown verbatim
     * because it is more helpful than a generic category; used only for 400/409/422 and
     * `success == false` bodies.
     */
    data class Message(val text: String) : AppError()

    /** Anything we couldn't classify. */
    data object Unknown : AppError()
}
