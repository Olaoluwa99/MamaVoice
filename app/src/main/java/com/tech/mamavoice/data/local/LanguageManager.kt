package com.tech.mamavoice.data.local

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for the app's language.
 *
 * A language choice drives two independent layers:
 *  - **UI text** — applied through [AppCompatDelegate.setApplicationLocales], which swaps the
 *    active resource locale and (with `autoStoreLocales` enabled in the manifest) persists and
 *    re-applies it across process restarts and Android 13+ system per-app language settings.
 *  - **AI / voice** — persisted as a code in DataStore via [SettingsManager] so the chat,
 *    STT and TTS layers can observe [appLanguage] and request the right language.
 *
 * Always change the language through [setLanguage] so both layers stay in sync.
 */
@Singleton
class LanguageManager @Inject constructor(
    private val settingsManager: SettingsManager
) {
    /** The currently selected language (defaults to English until the user chooses). */
    val appLanguage: Flow<AppLanguage> = settingsManager.appLanguage

    /** Whether the user has explicitly chosen a language yet (gates onboarding). */
    val isLanguageSelected: Flow<Boolean> = settingsManager.isLanguageSelected

    /** Persists [language] and applies it to the UI. Safe to call from any coroutine. */
    suspend fun setLanguage(language: AppLanguage) {
        settingsManager.setAppLanguage(language)
        withContext(Dispatchers.Main) { applyLocale(language) }
    }

    /** Applies [language] to the UI locale only. Must run on the main thread. */
    fun applyLocale(language: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(language.code)
        )
    }
}
