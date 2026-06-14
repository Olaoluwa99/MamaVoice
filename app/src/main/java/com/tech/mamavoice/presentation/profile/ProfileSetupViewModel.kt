package com.tech.mamavoice.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tech.mamavoice.domain.repository.AuthRepository
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
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _firstName = MutableStateFlow("")
    val firstName: StateFlow<String> = _firstName.asStateFlow()

    private val _profileType = MutableStateFlow("Pregnant") // "Pregnant" or "New Mom"
    val profileType: StateFlow<String> = _profileType.asStateFlow()

    private val _targetDate = MutableStateFlow("")
    val targetDate: StateFlow<String> = _targetDate.asStateFlow()

    private val _uiState = MutableStateFlow<ProfileSetupUiState>(ProfileSetupUiState.Idle)
    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()

    fun onFirstNameChange(name: String) {
        _firstName.update { name }
    }

    fun onProfileTypeChange(type: String) {
        _profileType.update { type }
    }

    fun onTargetDateChange(date: String) {
        _targetDate.update { date }
    }

    fun onCompleteProfile() {
        viewModelScope.launch {
            _uiState.update { ProfileSetupUiState.Loading }
            val result = authRepository.updateProfile(firstName.value, profileType.value, targetDate.value)
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
