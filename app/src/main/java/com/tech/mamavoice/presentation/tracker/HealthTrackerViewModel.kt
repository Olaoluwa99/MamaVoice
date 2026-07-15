package com.tech.mamavoice.presentation.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tech.mamavoice.domain.model.HealthLog
import com.tech.mamavoice.domain.repository.CoreFeaturesRepository
import com.tech.mamavoice.domain.util.AppError
import com.tech.mamavoice.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HealthTrackerUiState(
    val isLoading: Boolean = false,
    val logs: List<HealthLog> = emptyList(),
    val error: AppError? = null,
    val isSubmitting: Boolean = false,
    val submitError: AppError? = null
)

@HiltViewModel
class HealthTrackerViewModel @Inject constructor(
    private val repository: CoreFeaturesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HealthTrackerUiState())
    val state: StateFlow<HealthTrackerUiState> = _state.asStateFlow()

    init {
        getHealthLogs()
    }

    private fun getHealthLogs() {
        repository.getHealthLogs().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        logs = result.data ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.error ?: AppError.Unknown
                    )
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                }
            }
        }.launchIn(viewModelScope)
    }

    /** Re-fetches after a failure (wired to the error state's "Try again"). */
    fun retry() = getHealthLogs()

    fun submitLog(weight: Double?, bp: String?, nutrition: String?, symptoms: String?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, submitError = null)
            val result = repository.submitHealthLog(weight, bp, nutrition, symptoms)
            if (result is Resource.Success) {
                _state.value = _state.value.copy(isSubmitting = false)
                // In a real app, repository flow would update automatically
                // Or we can fetch again
                getHealthLogs()
            } else if (result is Resource.Error) {
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    submitError = result.error ?: AppError.Unknown
                )
            }
        }
    }
}
