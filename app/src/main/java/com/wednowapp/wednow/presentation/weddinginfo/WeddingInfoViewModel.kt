package com.wednowapp.wednow.presentation.weddinginfo

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

sealed class WeddingInfoState {
    object Loading : WeddingInfoState()
    data class Success(val wedding: Wedding) : WeddingInfoState()
    data class Error(val message: String) : WeddingInfoState()
}

@HiltViewModel
class WeddingInfoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getWeddingByIdUseCase: GetWeddingByIdUseCase,
) : ViewModel() {

    val weddingId: String = checkNotNull(savedStateHandle[Screen.WeddingInfo.ARG])

    private val _state = MutableStateFlow<WeddingInfoState>(WeddingInfoState.Loading)
    val state: StateFlow<WeddingInfoState> = _state.asStateFlow()

    init {
        loadWedding()
    }

    fun retry() {
        _state.value = WeddingInfoState.Loading
        loadWedding()
    }

    private fun loadWedding() {
        viewModelScope.launch {
            getWeddingByIdUseCase(weddingId)
                .onSuccess { wedding ->
                    _state.value = if (wedding != null)
                        WeddingInfoState.Success(wedding)
                    else
                        WeddingInfoState.Error("Wedding not found")
                }
                .onFailure { err ->
                    _state.value = WeddingInfoState.Error(err.message ?: "Failed to load")
                }
        }
    }
}
