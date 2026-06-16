package com.tech.mamavoice.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tech.mamavoice.domain.repository.AuthRepository
import com.tech.mamavoice.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun onLoginClick() {
        if (_email.value.isBlank() || _password.value.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authRepository.login(_email.value, _password.value)) {
                is Resource.Success -> _uiState.value = AuthUiState.LoginSuccess(result.data ?: false)
                is Resource.Error -> {
                    if (result.message == "NOT_VERIFIED") {
                        val resendResult = authRepository.resendOtp(_email.value)
                        if (resendResult is Resource.Success) {
                            _uiState.value = AuthUiState.NeedsVerification(email = _email.value, otpId = resendResult.data ?: "")
                        } else {
                            _uiState.value = AuthUiState.Error("Please verify your email. Failed to resend code.")
                        }
                    } else {
                        _uiState.value = AuthUiState.Error(result.message ?: "Login failed")
                    }
                }
                is Resource.Loading -> _uiState.value = AuthUiState.Loading
            }
        }
    }
}
