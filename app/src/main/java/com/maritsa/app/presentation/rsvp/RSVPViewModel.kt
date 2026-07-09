package com.maritsa.app.presentation.rsvp

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maritsa.app.core.navigation.Screen
import com.maritsa.app.domain.model.Guest
import com.maritsa.app.domain.model.GuestGroup
import com.maritsa.app.domain.repository.GuestGroupRepository
import com.maritsa.app.domain.usecase.GetCurrentGuestUseCase
import com.maritsa.app.domain.usecase.UpdateRsvpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
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
    private val updateRsvpUseCase: UpdateRsvpUseCase,
    private val guestGroupRepository: GuestGroupRepository,
) : ViewModel() {

    val weddingId: String = checkNotNull(savedStateHandle[Screen.RSVP.ARG])

    private val _guestLoaded = MutableStateFlow(false)
    val guestLoaded: StateFlow<Boolean> = _guestLoaded.asStateFlow()

    val currentGuest: StateFlow<Guest?> = getCurrentGuestUseCase(weddingId)
        .onEach { _guestLoaded.value = true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentGroup: StateFlow<GuestGroup?> = currentGuest
        .flatMapLatest { guest ->
            val groupId = guest?.groupId
            if (groupId != null) {
                guestGroupRepository.getGuestGroupById(weddingId, groupId)
            } else {
                flowOf(null)
            }
        }
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

    fun submitMemberRsvp(memberIndex: Int, status: String) {
        val group = currentGroup.value ?: return
        val updated = group.members.mapIndexed { i, m ->
            if (i == memberIndex) m.copy(rsvpStatus = status) else m
        }
        viewModelScope.launch {
            guestGroupRepository.updateGroupMembers(weddingId, group.id, updated)
        }
    }

    fun resetSubmitState() {
        _submitState.value = RsvpSubmitState.Idle
    }
}
