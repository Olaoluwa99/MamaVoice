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
    val error: String? = null,
    val selectedFood: FoodItem? = null
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
                    _state.value = _state.value.copy(
                        items = result.data ?: emptyList(),
                        isLoading = false,
                        error = null
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        error = result.message ?: "An unexpected error occurred",
                        isLoading = false
                    )
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun selectFood(food: FoodItem?) {
        _state.value = _state.value.copy(selectedFood = food)
    }
}
