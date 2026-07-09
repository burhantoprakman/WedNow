package com.maritsa.app.presentation.broadcast

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maritsa.app.core.navigation.Screen
import com.maritsa.app.domain.model.Broadcast
import com.maritsa.app.domain.model.GuestRole
import com.maritsa.app.domain.usecase.GetBroadcastsUseCase
import com.maritsa.app.domain.usecase.GetCurrentGuestUseCase
import com.maritsa.app.domain.usecase.SendBroadcastUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BroadcastViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getBroadcastsUseCase: GetBroadcastsUseCase,
    getCurrentGuestUseCase: GetCurrentGuestUseCase,
    private val sendBroadcastUseCase: SendBroadcastUseCase
) : ViewModel() {

    val weddingId: String = checkNotNull(savedStateHandle[Screen.Broadcasts.ARG])

    val broadcasts: StateFlow<List<Broadcast>> = getBroadcastsUseCase(weddingId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val canSendBroadcast: StateFlow<Boolean> = getCurrentGuestUseCase(weddingId)
        .map { guest -> guest?.role == GuestRole.ADMIN || guest?.role == GuestRole.COADMIN }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    var composeText by mutableStateOf("")
        private set
    var isDialogVisible by mutableStateOf(false)
        private set
    var isSending by mutableStateOf(false)
        private set
    var sendError by mutableStateOf<String?>(null)
        private set

    val canConfirmSend: Boolean get() = composeText.isNotBlank() && !isSending

    fun onComposeTextChange(text: String) {
        composeText = text
    }

    fun showDialog() {
        composeText = ""
        sendError = null
        isDialogVisible = true
    }

    fun dismissDialog() {
        isDialogVisible = false
        composeText = ""
        sendError = null
    }

    fun sendBroadcast() {
        if (!canConfirmSend) return
        viewModelScope.launch {
            isSending = true
            sendError = null
            sendBroadcastUseCase(weddingId, composeText)
                .onSuccess { dismissDialog() }
                .onFailure { sendError = it.message ?: "Failed to send announcement" }
            isSending = false
        }
    }
}
