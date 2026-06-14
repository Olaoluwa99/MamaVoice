package com.tech.mamavoice.presentation.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tech.mamavoice.domain.model.FoodItem
import com.tech.mamavoice.domain.repository.CoreFeaturesRepository
import com.tech.mamavoice.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class FoodUiState(
    val isLoading: Boolean = false,
    val items: List<FoodItem> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class FoodViewModel @Inject constructor(
    private val repository: CoreFeaturesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FoodUiState())
    val state: StateFlow<FoodUiState> = _state.asStateFlow()

    init {
        getFoodItems()
    }

    private fun getFoodItems() {
        repository.getFoodItems().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.value = FoodUiState(
                        items = result.data ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    _state.value = FoodUiState(
                        error = result.message ?: "An unexpected error occurred"
                    )
                }
                is Resource.Loading -> {
                    _state.value = FoodUiState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }
}
