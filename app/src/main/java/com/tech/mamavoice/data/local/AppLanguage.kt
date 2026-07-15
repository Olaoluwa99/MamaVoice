package com.tech.mamavoice.data.local

/**
 * Languages supported by MamaVoice.
 *
 * [code] is the canonical BCP-47 / ISO language tag used everywhere — Android resource qualifiers
 * (via [com.tech.mamavoice.presentation.language.ProvideAppLocale]) and the speech (STT/TTS) + AI
 * layer. These intentionally match Spitch's language codes so a single choice drives the whole app.
 *
 * [displayName] is the language's name in its own language, so it reads correctly on the
 * onboarding picker before any locale has been applied.
 *
 * [apiLang] is the value the backend's `lang` query param expects (its English name), or `null`
 * for languages the API doesn't localize content into, in which case the request omits `lang`
 * and the server falls back to the profile language, then English.
 */
enum class AppLanguage(val code: String, val displayName: String, val apiLang: String?) {
    ENGLISH("en", "English", "English"),
    PIDGIN("pcm", "Pidgin", "Pidgin"),
    YORUBA("yo", "Yorùbá", "Yoruba"),
    IGBO("ig", "Igbo", "Igbo"),
    HAUSA("ha", "Hausa", "Hausa");

    companion object {
        /** Resolves a stored code back to a language, defaulting to [ENGLISH]. */
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: ENGLISH
    }
}
