package com.tech.mamavoice.presentation.immunization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tech.mamavoice.domain.model.VaccineItem
import com.tech.mamavoice.domain.repository.CoreFeaturesRepository
import com.tech.mamavoice.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ImmunizationUiState(
    val isLoading: Boolean = false,
    val vaccines: List<VaccineItem> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ImmunizationViewModel @Inject constructor(
    private val repository: CoreFeaturesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ImmunizationUiState())
    val state: StateFlow<ImmunizationUiState> = _state.asStateFlow()

    init {
        getImmunizationTimeline()
    }

    private fun getImmunizationTimeline() {
        repository.getImmunizationTimeline().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.value = ImmunizationUiState(
                        vaccines = result.data ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    _state.value = ImmunizationUiState(
                        error = result.message ?: "An unexpected error occurred"
                    )
                }
                is Resource.Loading -> {
                    _state.value = ImmunizationUiState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun logVaccine(
        vaccineId: String,
        vaccineName: String,
        date: String,
        isCompleted: Boolean,
        sideEffects: String?
    ) {
        viewModelScope.launch {
            val result = repository.markVaccineCompleted(
                vaccineId = vaccineId,
                vaccineName = vaccineName,
                date = date,
                isCompleted = isCompleted,
                sideEffects = sideEffects
            )
            if (result is Resource.Success) {
                // Optimistically update the state.
                val updatedVaccines = _state.value.vaccines.map {
                    if (it.id == vaccineId) {
                        it.copy(
                            isCompleted = isCompleted,
                            administeredDate = date,
                            sideEffects = sideEffects
                        )
                    } else it
                }
                _state.value = _state.value.copy(vaccines = updatedVaccines)
            } else if (result is Resource.Error) {
                _state.value = _state.value.copy(error = result.message)
            }
        }
    }
}
