package com.wednowapp.wednow.presentation.chat

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wednowapp.wednow.core.navigation.Screen
import com.wednowapp.wednow.core.session.GuestSessionManager
import com.wednowapp.wednow.domain.model.DirectMessage
import com.wednowapp.wednow.domain.model.Guest
import com.wednowapp.wednow.domain.usecase.GetDirectMessagesUseCase
import com.wednowapp.wednow.domain.usecase.GetGuestByIdUseCase
import com.wednowapp.wednow.domain.usecase.SendDirectMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DirectMessageViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getDirectMessagesUseCase: GetDirectMessagesUseCase,
    getGuestByIdUseCase: GetGuestByIdUseCase,
    private val sendDirectMessageUseCase: SendDirectMessageUseCase,
    @ApplicationContext context: Context
) : ViewModel() {

    val weddingId: String = checkNotNull(savedStateHandle[Screen.DirectMessage.WEDDING_ARG])
    val otherGuestId: String = checkNotNull(savedStateHandle[Screen.DirectMessage.OTHER_GUEST_ARG])

    val myGuestId: String = GuestSessionManager.getGuestId(context)

    val channelId: String = listOf(myGuestId, otherGuestId).sorted().joinToString("_")

    val messages: StateFlow<List<DirectMessage>> = getDirectMessagesUseCase(weddingId, channelId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val otherGuest: StateFlow<Guest?> = getGuestByIdUseCase(weddingId, otherGuestId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    var inputText by mutableStateOf("")
        private set

    val canSend: Boolean get() = inputText.isNotBlank()

    fun onInputChange(text: String) { inputText = text }

    fun sendMessage() {
        if (!canSend) return
        val text = inputText
        inputText = ""
        viewModelScope.launch {
            sendDirectMessageUseCase(weddingId, channelId, text)
        }
    }
}
