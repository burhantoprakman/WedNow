package com.wednowapp.wednow.presentation.rsvp

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wednowapp.wednow.core.navigation.Screen
import com.wednowapp.wednow.domain.model.Guest
import com.wednowapp.wednow.domain.usecase.GetCurrentGuestUseCase
import com.wednowapp.wednow.domain.usecase.UpdateRsvpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RsvpSubmitState {
    object Idle : RsvpSubmitState()
    object Loading : RsvpSubmitState()
    object Success : RsvpSubmitState()
    data class Error(val message: String) : RsvpSubmitState()
}

@HiltViewModel
class RSVPViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCurrentGuestUseCase: GetCurrentGuestUseCase,
    private val updateRsvpUseCase: UpdateRsvpUseCase
) : ViewModel() {

    val weddingId: String = checkNotNull(savedStateHandle[Screen.RSVP.ARG])

    val currentGuest: StateFlow<Guest?> = getCurrentGuestUseCase(weddingId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _submitState = MutableStateFlow<RsvpSubmitState>(RsvpSubmitState.Idle)
    val submitState: StateFlow<RsvpSubmitState> = _submitState.asStateFlow()

    fun submit(status: String) {
        if (_submitState.value == RsvpSubmitState.Loading) return
        viewModelScope.launch {
            _submitState.value = RsvpSubmitState.Loading
            val result = updateRsvpUseCase(weddingId, status)
            _submitState.value = result.fold(
                onSuccess = { RsvpSubmitState.Success },
                onFailure = { RsvpSubmitState.Error(it.message ?: "Failed to save RSVP") }
            )
        }
    }

    fun resetSubmitState() {
        _submitState.value = RsvpSubmitState.Idle
    }
}
