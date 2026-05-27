package com.wednowapp.wednow.presentation.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wednowapp.wednow.core.navigation.Screen
import com.wednowapp.wednow.domain.usecase.JoinWeddingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JoinWeddingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val joinWeddingUseCase: JoinWeddingUseCase,
) : ViewModel() {

    /**
     * Pre-filled when the screen is reached via a QR deep link
     * (route = join_wedding_deep/{code}). Empty string for the manual-entry flow.
     */
    var weddingCode by mutableStateOf(
        savedStateHandle.get<String>(Screen.JoinWedding.CODE_ARG).orEmpty()
    )
        private set
    var guestName by mutableStateOf("")
        private set

    private val _state = MutableStateFlow<JoinWeddingState>(JoinWeddingState.Idle)
    val state: StateFlow<JoinWeddingState> = _state.asStateFlow()

    val isSubmitEnabled: Boolean
        get() = weddingCode.isNotBlank()

    fun onWeddingCodeChange(value: String) { weddingCode = value }
    fun onGuestNameChange(value: String) { guestName = value }

    fun submit() {
        viewModelScope.launch {
            _state.value = JoinWeddingState.Loading
            joinWeddingUseCase(
                weddingId = weddingCode.trim(),
                guestName = guestName.trim().takeIf { it.isNotBlank() }
            ).onSuccess { weddingId ->
                _state.value = JoinWeddingState.Success(weddingId)
            }.onFailure { error ->
                _state.value = JoinWeddingState.Error(error.message ?: "Failed to join wedding")
            }
        }
    }
}

sealed class JoinWeddingState {
    object Idle : JoinWeddingState()
    object Loading : JoinWeddingState()
    data class Success(val weddingId: String) : JoinWeddingState()
    data class Error(val message: String) : JoinWeddingState()
}
