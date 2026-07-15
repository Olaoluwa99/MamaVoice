package com.tech.mamavoice.data.local

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for the app's language.
 *
 * The choice is persisted as a code in DataStore via [SettingsManager]. Both layers observe it:
 *  - **UI text** — [com.tech.mamavoice.presentation.language.ProvideAppLocale] overrides the Compose
 *    locale from [appLanguage], so switching updates all text in place with no Activity recreation.
 *  - **AI / voice** — the chat, STT and TTS layers observe [appLanguage] to request the right
 *    language (see the `lang` query param and the voice pipeline).
 *
 * Always change the language through [setLanguage] so every observer stays in sync.
 */
@Singleton
class LanguageManager @Inject constructor(
    private val settingsManager: SettingsManager
) {
    /** The currently selected language (defaults to English until the user chooses). */
    val appLanguage: Flow<AppLanguage> = settingsManager.appLanguage

    /** Whether the user has explicitly chosen a language yet (gates onboarding). */
    val isLanguageSelected: Flow<Boolean> = settingsManager.isLanguageSelected

    /** Persists [language]; observers of [appLanguage] (UI locale, AI/voice) react to the change. */
    suspend fun setLanguage(language: AppLanguage) {
        settingsManager.setAppLanguage(language)
    }
}
