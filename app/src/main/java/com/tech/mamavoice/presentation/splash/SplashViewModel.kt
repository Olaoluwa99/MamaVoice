package com.tech.mamavoice.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tech.mamavoice.data.local.SettingsManager
import com.tech.mamavoice.data.local.TokenManager
import com.tech.mamavoice.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        determineStartDestination()
    }

    private fun determineStartDestination() {
        viewModelScope.launch {
            delay(1500)

            // First run: let the user pick a language before anything else.
            if (!settingsManager.isLanguageSelected.first()) {
                _startDestination.value = Screen.LanguageSelection.route
                return@launch
            }

            val token = tokenManager.authToken.firstOrNull()
            val isExistingUser = tokenManager.isExistingUser.first()

            _startDestination.value = if (token.isNullOrEmpty()) {
                Screen.Welcome.route
            } else if (!isExistingUser) {
                Screen.ProfileSetup.route
            } else {
                Screen.Main.route
            }
        }
    }
}
