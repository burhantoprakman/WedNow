package com.wednowapp.wednow.presentation.guestbook

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wednowapp.wednow.core.navigation.Screen
import com.wednowapp.wednow.domain.model.GuestbookPost
import com.wednowapp.wednow.domain.usecase.AddGuestbookPostUseCase
import com.wednowapp.wednow.domain.usecase.GetGuestbookPostsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PostSubmitState {
    object Idle : PostSubmitState()
    object Loading : PostSubmitState()
    object Success : PostSubmitState()
    data class Error(val message: String) : PostSubmitState()
}

@HiltViewModel
class GuestbookViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getGuestbookPostsUseCase: GetGuestbookPostsUseCase,
    private val addGuestbookPostUseCase: AddGuestbookPostUseCase
) : ViewModel() {

    val weddingId: String = checkNotNull(savedStateHandle[Screen.Guestbook.ARG])

    val posts: StateFlow<List<GuestbookPost>> = getGuestbookPostsUseCase(weddingId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var messageInput by mutableStateOf("")
        private set

    private val _submitState = MutableStateFlow<PostSubmitState>(PostSubmitState.Idle)
    val submitState: StateFlow<PostSubmitState> = _submitState.asStateFlow()

    val canSubmit get() = messageInput.isNotBlank() && _submitState.value != PostSubmitState.Loading

    fun onMessageChange(value: String) {
        messageInput = value
    }

    fun submit() {
        if (!canSubmit) return
        viewModelScope.launch {
            _submitState.value = PostSubmitState.Loading
            val result = addGuestbookPostUseCase(weddingId, messageInput)
            _submitState.value = result.fold(
                onSuccess = { messageInput = ""; PostSubmitState.Success },
                onFailure = { PostSubmitState.Error(it.message ?: "Failed to post message") }
            )
        }
    }

    fun resetSubmitState() {
        _submitState.value = PostSubmitState.Idle
    }
}
