package com.tech.mamavoice.data.local

/**
 * Languages supported by MamaVoice.
 *
 * [code] is the canonical BCP-47 / ISO language tag used everywhere — Android resource
 * qualifiers, [androidx.appcompat.app.AppCompatDelegate.setApplicationLocales], and the
 * speech (STT/TTS) + AI layer. These intentionally match Spitch's language codes so a
 * single choice drives the whole app.
 *
 * [displayName] is the language's name in its own language, so it reads correctly on the
 * onboarding picker before any locale has been applied.
 */
enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    PIDGIN("pcm", "Pidgin"),
    YORUBA("yo", "Yorùbá"),
    IGBO("ig", "Igbo"),
    HAUSA("ha", "Hausa");

    companion object {
        /** Resolves a stored code back to a language, defaulting to [ENGLISH]. */
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: ENGLISH
    }
}
