package com.tech.mamavoice.presentation.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tech.mamavoice.R
import com.tech.mamavoice.domain.repository.AuthRepository
import com.tech.mamavoice.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PasswordRule(@StringRes val descriptionRes: Int) {
    MIN_LENGTH(R.string.pwd_rule_min_length),
    UPPERCASE(R.string.pwd_rule_uppercase),
    NUMBER(R.string.pwd_rule_number),
    SPECIAL_CHAR(R.string.pwd_rule_special)
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val unfulfilledRules: StateFlow<List<PasswordRule>> = _password.map { pwd ->
        val unfulfilled = mutableListOf<PasswordRule>()
        if (pwd.length < 8) unfulfilled.add(PasswordRule.MIN_LENGTH)
        if (!pwd.any { it.isUpperCase() }) unfulfilled.add(PasswordRule.UPPERCASE)
        if (!pwd.any { it.isDigit() }) unfulfilled.add(PasswordRule.NUMBER)
        if (!pwd.any { !it.isLetterOrDigit() }) unfulfilled.add(PasswordRule.SPECIAL_CHAR)
        unfulfilled
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PasswordRule.entries.toList())

    val isPasswordValid: StateFlow<Boolean> = combine(_password, unfulfilledRules) { pwd, rules ->
        pwd.isNotEmpty() && rules.isEmpty()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun onSignUpClick() {
        if (!isPasswordValid.value || _email.value.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authRepository.register(_email.value, _password.value)) {
                is Resource.Success -> _uiState.value = AuthUiState.RegisterSuccess(
                    email = _email.value,
                    otpId = result.data ?: ""
                )
                is Resource.Error -> _uiState.value = AuthUiState.Error(result.message ?: "Registration failed")
                is Resource.Loading -> _uiState.value = AuthUiState.Loading
            }
        }
    }
}
