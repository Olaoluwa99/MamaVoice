package com.tech.mamavoice.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tech.mamavoice.data.remote.dto.AppEnumsResponse
import com.tech.mamavoice.domain.repository.AuthRepository
import com.tech.mamavoice.domain.repository.MamaVoiceRepository
import com.tech.mamavoice.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProfileSetupUiState {
    object Idle : ProfileSetupUiState
    object Loading : ProfileSetupUiState
    object Success : ProfileSetupUiState
    data class Error(val message: String) : ProfileSetupUiState
}

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val mamaVoiceRepository: MamaVoiceRepository
) : ViewModel() {

    private val _appEnums = MutableStateFlow(AppEnumsResponse())
    val appEnums: StateFlow<AppEnumsResponse> = _appEnums.asStateFlow()

    private val _firstName = MutableStateFlow("")
    val firstName: StateFlow<String> = _firstName.asStateFlow()

    private val _lastName = MutableStateFlow("")
    val lastName: StateFlow<String> = _lastName.asStateFlow()

    private val _motherStage = MutableStateFlow("")
    val motherStage: StateFlow<String> = _motherStage.asStateFlow()

    private val _language = MutableStateFlow("")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _state = MutableStateFlow("")
    val state: StateFlow<String> = _state.asStateFlow()

    private val _lga = MutableStateFlow("")
    val lga: StateFlow<String> = _lga.asStateFlow()

    private val _targetDate = MutableStateFlow("")
    val targetDate: StateFlow<String> = _targetDate.asStateFlow()

    private val _uiState = MutableStateFlow<ProfileSetupUiState>(ProfileSetupUiState.Idle)
    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()

    init {
        fetchEnums()
    }

    private fun fetchEnums() {
        viewModelScope.launch {
            when (val result = mamaVoiceRepository.getAppEnums()) {
                is Resource.Success -> {
                    result.data?.let { _appEnums.update { _ -> it } }
                }
                else -> Unit
            }
        }
    }

    fun onFirstNameChange(name: String) { _firstName.update { name } }
    fun onLastNameChange(name: String) { _lastName.update { name } }
    fun onMotherStageChange(stage: String) { _motherStage.update { stage } }
    fun onLanguageChange(lang: String) { _language.update { lang } }
    
    fun onStateChange(newState: String) {
        _state.update { newState }
        _lga.update { "" } // Reset LGA when state changes
    }
    
    fun onLgaChange(newLga: String) { _lga.update { newLga } }
    fun onTargetDateChange(date: String) { _targetDate.update { date } }

    fun onCompleteProfile() {
        viewModelScope.launch {
            _uiState.update { ProfileSetupUiState.Loading }
            val result = authRepository.updateProfile(
                firstName = firstName.value,
                lastName = lastName.value,
                language = language.value,
                state = state.value,
                lga = lga.value,
                motherStage = motherStage.value,
                targetDate = targetDate.value
            )
            when (result) {
                is Resource.Success -> {
                    _uiState.update { ProfileSetupUiState.Success }
                }
                is Resource.Error -> {
                    _uiState.update { ProfileSetupUiState.Error(result.message ?: "Failed to update profile") }
                }
                else -> Unit
            }
        }
    }
}
