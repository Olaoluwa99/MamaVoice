package com.tech.mamavoice.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class AppTheme {
    SYSTEM, LIGHT, DARK
}

private val Context.settingsDataStore by preferencesDataStore(name = "mama_voice_settings")

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val THEME_KEY = stringPreferencesKey("app_theme")
        private val LANGUAGE_KEY = stringPreferencesKey("app_language")
        private val ONBOARDING_SEEN_KEY = booleanPreferencesKey("has_seen_onboarding")
    }

    val appTheme: Flow<AppTheme> = context.settingsDataStore.data.map { preferences ->
        val themeString = preferences[THEME_KEY] ?: AppTheme.SYSTEM.name
        try {
            AppTheme.valueOf(themeString)
        } catch (e: IllegalArgumentException) {
            AppTheme.SYSTEM
        }
    }

    suspend fun setAppTheme(theme: AppTheme) {
        context.settingsDataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }

    /** The user's chosen language; defaults to [AppLanguage.ENGLISH] until one is picked. */
    val appLanguage: Flow<AppLanguage> = context.settingsDataStore.data.map { preferences ->
        AppLanguage.fromCode(preferences[LANGUAGE_KEY])
    }

    /**
     * Whether the user has ever explicitly chosen a language. Used to gate the onboarding
     * language-selection step (distinct from the default English fall-back above).
     */
    val isLanguageSelected: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] != null
    }

    suspend fun setAppLanguage(language: AppLanguage) {
        context.settingsDataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language.code
        }
    }

    /** Whether the first-run intro carousel has been shown. Gates the onboarding step. */
    val hasSeenOnboarding: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[ONBOARDING_SEEN_KEY] ?: false
    }

    suspend fun setOnboardingSeen() {
        context.settingsDataStore.edit { preferences ->
            preferences[ONBOARDING_SEEN_KEY] = true
        }
    }
}
