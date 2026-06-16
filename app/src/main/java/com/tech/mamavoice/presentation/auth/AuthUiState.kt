package com.tech.mamavoice.presentation.auth

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class RegisterSuccess(val email: String, val otpId: String) : AuthUiState
    data class LoginSuccess(val isProfileCompleted: Boolean) : AuthUiState
    data class Error(val message: String) : AuthUiState
}
