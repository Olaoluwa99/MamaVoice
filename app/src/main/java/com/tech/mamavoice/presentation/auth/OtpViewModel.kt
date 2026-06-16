package com.tech.mamavoice.presentation.auth

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

@HiltViewModel
class OtpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OtpUiState())
    val uiState: StateFlow<OtpUiState> = _uiState.asStateFlow()

    fun onOtpChange(value: String) {
        _uiState.update {
            it.copy(
                otp = value.filter(Char::isDigit).take(6),
                errorMessage = null
            )
        }
    }

    private var currentOtpId: String = ""
    private var userEmail: String = ""

    fun initializeData(email: String, initialOtpId: String) {
        if (userEmail.isEmpty()) {
            userEmail = email
            currentOtpId = initialOtpId
        }
    }

    fun verifyOtp() {
        if (_uiState.value.otp.length != 6) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.verifyOtp(currentOtpId, _uiState.value.otp)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true, isProfileCompleted = result.data ?: false) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun onResendClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.resendOtp(userEmail)) {
                is Resource.Success -> {
                    currentOtpId = result.data ?: currentOtpId
                    _uiState.update { it.copy(isLoading = false, otp = "", errorMessage = "A new verification OTP has been sent to your email") }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is Resource.Loading -> Unit
            }
        }
    }
}

data class OtpUiState(
    val otp: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isProfileCompleted: Boolean = false,
    val errorMessage: String? = null
)
