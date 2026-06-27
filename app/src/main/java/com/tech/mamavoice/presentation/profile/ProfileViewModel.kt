package com.tech.mamavoice.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tech.mamavoice.data.local.AppLanguage
import com.tech.mamavoice.data.local.AppTheme
import com.tech.mamavoice.data.local.LanguageManager
import com.tech.mamavoice.data.local.SettingsManager
import com.tech.mamavoice.data.local.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val currentTheme: AppTheme = AppTheme.SYSTEM,
    val currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    val userName: String = "Mama"
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    private val languageManager: LanguageManager,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val theme = settingsManager.appTheme.first()
            val language = languageManager.appLanguage.first()
            _state.value = _state.value.copy(currentTheme = theme, currentLanguage = language)

            // In a real implementation we would fetch the user's name from a database or API here.
            // For now, we set a default name.
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsManager.setAppTheme(theme)
            _state.value = _state.value.copy(currentTheme = theme)
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            languageManager.setLanguage(language)
            _state.value = _state.value.copy(currentLanguage = language)
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            tokenManager.clearSession()
            onLoggedOut()
        }
    }
}
