package com.wednowapp.wednow.presentation.timeline

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

sealed class TimelineState {
    object Loading : TimelineState()
    data class Success(val wedding: Wedding) : TimelineState()
    data class Error(val message: String) : TimelineState()
}

@HiltViewModel
class TimelineViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getWeddingByIdUseCase: GetWeddingByIdUseCase,
) : ViewModel() {

    private val weddingId: String = checkNotNull(savedStateHandle[Screen.Timeline.ARG])

    private val _state = MutableStateFlow<TimelineState>(TimelineState.Loading)
    val state: StateFlow<TimelineState> = _state.asStateFlow()

    init { loadWedding() }

    fun retry() {
        _state.value = TimelineState.Loading
        loadWedding()
    }

    private fun loadWedding() {
        viewModelScope.launch {
            getWeddingByIdUseCase(weddingId)
                .onSuccess { wedding ->
                    _state.value = if (wedding != null) {
                        TimelineState.Success(wedding)
                    } else {
                        TimelineState.Error("Wedding not found")
                    }
                }
                .onFailure { e ->
                    _state.value = TimelineState.Error(e.message ?: "Failed to load")
                }
        }
    }
}
