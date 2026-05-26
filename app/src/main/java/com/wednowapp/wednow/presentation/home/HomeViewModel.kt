package com.wednowapp.wednow.presentation.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wednowapp.wednow.core.navigation.Screen
import com.wednowapp.wednow.domain.model.Wedding
import com.wednowapp.wednow.domain.usecase.GetWeddingByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getWeddingByIdUseCase: GetWeddingByIdUseCase
) : ViewModel() {

    val weddingId: String = checkNotNull(savedStateHandle[Screen.WeddingHome.ARG])

    private val _state = MutableStateFlow<WeddingDetailState>(WeddingDetailState.Loading)
    val state: StateFlow<WeddingDetailState> = _state.asStateFlow()

    init {
        loadWedding()
    }

    fun retry() {
        _state.value = WeddingDetailState.Loading
        loadWedding()
    }

    private fun loadWedding() {
        viewModelScope.launch {
            getWeddingByIdUseCase(weddingId)
                .onSuccess { wedding ->
                    _state.value = if (wedding != null) {
                        WeddingDetailState.Success(wedding)
                    } else {
                        WeddingDetailState.Error("Wedding not found")
                    }
                }
                .onFailure { error ->
                    _state.value = WeddingDetailState.Error(
                        error.message ?: "Failed to load wedding details"
                    )
                }
        }
    }
}

sealed class WeddingDetailState {
    object Loading : WeddingDetailState()
    data class Success(val wedding: Wedding) : WeddingDetailState()
    data class Error(val message: String) : WeddingDetailState()
}
