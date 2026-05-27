package com.wednowapp.wednow.presentation.guestbook

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wednowapp.wednow.core.navigation.Screen
import com.wednowapp.wednow.core.session.GuestSessionManager
import com.wednowapp.wednow.domain.model.GuestbookPost
import com.wednowapp.wednow.domain.usecase.AddGuestbookPostUseCase
import com.wednowapp.wednow.domain.usecase.GetGuestbookPostsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    private val addGuestbookPostUseCase: AddGuestbookPostUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val weddingId: String = checkNotNull(savedStateHandle[Screen.Guestbook.ARG])

    /** Null = first Firestore snapshot not yet received (loading). */
    val posts: StateFlow<List<GuestbookPost>?> = getGuestbookPostsUseCase(weddingId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = null)

    /** Display name shown in the memory card preview. */
    val senderName: String = GuestSessionManager.getGuestName(context).ifBlank { "A Guest" }

    var messageInput by mutableStateOf("")
        private set

    var selectedPhotoUris by mutableStateOf<List<Uri>>(emptyList())
        private set

    private val _submitState = MutableStateFlow<PostSubmitState>(PostSubmitState.Idle)
    val submitState: StateFlow<PostSubmitState> = _submitState.asStateFlow()

    val canSubmit get() = messageInput.isNotBlank() && _submitState.value != PostSubmitState.Loading

    fun onMessageChange(value: String) {
        messageInput = value
    }

    /** Appends [uris] to the selection — total is capped at 3. */
    fun onPhotosSelected(uris: List<Uri>) {
        selectedPhotoUris = (selectedPhotoUris + uris).take(3)
    }

    fun onPhotoRemoved(uri: Uri) {
        selectedPhotoUris = selectedPhotoUris.filter { it != uri }
    }

    fun submit() {
        if (!canSubmit) return
        viewModelScope.launch {
            _submitState.value = PostSubmitState.Loading
            val result = addGuestbookPostUseCase(weddingId, messageInput, selectedPhotoUris)
            _submitState.value = result.fold(
                onSuccess = {
                    messageInput = ""
                    selectedPhotoUris = emptyList()
                    PostSubmitState.Success
                },
                onFailure = { PostSubmitState.Error(it.message ?: "Failed to post memory") }
            )
        }
    }

    fun resetSubmitState() {
        _submitState.value = PostSubmitState.Idle
    }
}
