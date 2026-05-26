package com.wednowapp.wednow.presentation.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wednowapp.wednow.core.navigation.Screen
import com.wednowapp.wednow.domain.model.ChatMessage
import com.wednowapp.wednow.domain.model.Guest
import com.wednowapp.wednow.domain.usecase.GetChatMessagesUseCase
import com.wednowapp.wednow.domain.usecase.GetCurrentGuestUseCase
import com.wednowapp.wednow.domain.usecase.GetGuestsUseCase
import com.wednowapp.wednow.domain.usecase.SendChatMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getChatMessagesUseCase: GetChatMessagesUseCase,
    getCurrentGuestUseCase: GetCurrentGuestUseCase,
    getGuestsUseCase: GetGuestsUseCase,
    private val sendChatMessageUseCase: SendChatMessageUseCase
) : ViewModel() {

    val weddingId: String = checkNotNull(savedStateHandle[Screen.Chat.ARG])

    val messages: StateFlow<List<ChatMessage>> = getChatMessagesUseCase(weddingId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val currentGuestFlow = getCurrentGuestUseCase(weddingId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val currentGuestId: StateFlow<String?> = currentGuestFlow
        .map { it?.id }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // DM tab: all guests except self
    val otherGuests: StateFlow<List<Guest>> = getGuestsUseCase(weddingId)
        .map { guests -> guests.filter { it.id != currentGuestFlow.value?.id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var inputText by mutableStateOf("")
        private set

    val canSend: Boolean get() = inputText.isNotBlank()

    fun onInputChange(text: String) { inputText = text }

    fun sendMessage() {
        if (!canSend) return
        val text = inputText
        inputText = ""
        val guestName = currentGuestFlow.value?.name.orEmpty()
        viewModelScope.launch {
            sendChatMessageUseCase(weddingId, text, guestName)
        }
    }
}
