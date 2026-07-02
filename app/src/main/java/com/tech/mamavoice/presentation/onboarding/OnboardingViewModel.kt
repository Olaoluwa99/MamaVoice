package com.tech.mamavoice.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tech.mamavoice.data.local.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsManager: SettingsManager
) : ViewModel() {

    /** Marks the intro carousel as seen so it never shows again on this install. */
    fun completeOnboarding() {
        viewModelScope.launch {
            settingsManager.setOnboardingSeen()
        }
    }
}
