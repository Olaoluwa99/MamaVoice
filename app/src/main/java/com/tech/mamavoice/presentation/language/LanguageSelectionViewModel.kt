package com.tech.mamavoice.presentation.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tech.mamavoice.data.local.AppLanguage
import com.tech.mamavoice.data.local.LanguageManager
import com.tech.mamavoice.data.local.TokenManager
import com.tech.mamavoice.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageSelectionViewModel @Inject constructor(
    private val languageManager: LanguageManager,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _navigateTo = MutableStateFlow<String?>(null)
    val navigateTo: StateFlow<String?> = _navigateTo.asStateFlow()

    init {
        // Applying a new locale recreates the activity. If that happens mid-onboarding the
        // language is already persisted, so resume by routing straight to the next screen.
        viewModelScope.launch {
            if (languageManager.isLanguageSelected.first()) {
                emitDestination()
            }
        }
    }

    fun onLanguageConfirmed(language: AppLanguage) {
        viewModelScope.launch {
            languageManager.setLanguage(language)
            emitDestination()
        }
    }

    /** Routes to the same place the splash would, now that a language has been chosen. */
    private suspend fun emitDestination() {
        val token = tokenManager.authToken.firstOrNull()
        val isExistingUser = tokenManager.isExistingUser.first()
        _navigateTo.value = when {
            token.isNullOrEmpty() -> Screen.Welcome.route
            !isExistingUser -> Screen.ProfileSetup.route
            else -> Screen.Main.route
        }
    }
}
