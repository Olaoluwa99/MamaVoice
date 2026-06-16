package com.tech.mamavoice.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "mama_voice_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val IS_EXISTING_USER = booleanPreferencesKey("is_existing_user")
    }

    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[AUTH_TOKEN]
    }

    val refreshToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[REFRESH_TOKEN]
    }

    val isExistingUser: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_EXISTING_USER] ?: false
    }

    suspend fun saveAuthData(accessToken: String, refreshTokenStr: String, isExistingUser: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshTokenStr
            preferences[IS_EXISTING_USER] = isExistingUser
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
