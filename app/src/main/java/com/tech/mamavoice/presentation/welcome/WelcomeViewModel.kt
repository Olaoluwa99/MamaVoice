package com.tech.mamavoice.presentation.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tech.mamavoice.data.local.AppLanguage
import com.tech.mamavoice.data.local.LanguageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val languageManager: LanguageManager
) : ViewModel() {

    private val _language = MutableStateFlow(AppLanguage.ENGLISH)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    init {
        viewModelScope.launch {
            languageManager.appLanguage.collect { _language.value = it }
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { languageManager.setLanguage(language) }
    }
}
