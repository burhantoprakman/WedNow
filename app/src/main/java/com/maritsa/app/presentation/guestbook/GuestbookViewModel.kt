package com.maritsa.app.presentation.guestbook

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maritsa.app.core.identity.IdentityManager
import com.maritsa.app.core.navigation.Screen
import com.maritsa.app.core.session.GuestSessionManager
import com.maritsa.app.domain.model.ContentPermissions
import com.maritsa.app.domain.model.GuestbookPost
import com.maritsa.app.domain.usecase.AddGuestbookPostUseCase
import com.maritsa.app.domain.usecase.DeleteGuestbookPostUseCase
import com.maritsa.app.domain.usecase.GetCurrentGuestUseCase
import com.maritsa.app.domain.usecase.GetGuestbookPostsUseCase
import com.maritsa.app.domain.usecase.UpdateGuestbookPostUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
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
    getCurrentGuestUseCase: GetCurrentGuestUseCase,
    private val addGuestbookPostUseCase: AddGuestbookPostUseCase,
    private val deleteGuestbookPostUseCase: DeleteGuestbookPostUseCase,
    private val updateGuestbookPostUseCase: UpdateGuestbookPostUseCase,
    private val identityManager: IdentityManager,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val weddingId: String = checkNotNull(savedStateHandle[Screen.Guestbook.ARG])

    /**
     * Unified identity ID — UUID for anonymous guests, Firebase UID for signed-in users.
     * Replaces the legacy [currentUserId] (Firebase-UID-only) pattern.
     */
    val currentIdentityId: String get() = identityManager.currentIdentityId

    val posts: StateFlow<List<GuestbookPost>?> = getGuestbookPostsUseCase(weddingId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = null)

    val currentGuestRole: StateFlow<String?> = getCurrentGuestUseCase(weddingId)
        .map { it?.role }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = null)

    val senderName: String = GuestSessionManager.getGuestName(context).ifBlank { "A Guest" }

    var messageInput by mutableStateOf("")
        private set
    var selectedPhotoUris by mutableStateOf<List<Uri>>(emptyList())
        private set

    private val _submitState = MutableStateFlow<PostSubmitState>(PostSubmitState.Idle)
    val submitState: StateFlow<PostSubmitState> = _submitState.asStateFlow()

    val canSubmit get() = messageInput.isNotBlank() && _submitState.value != PostSubmitState.Loading

    // ── Delete flow ───────────────────────────────────────────────────────────
    var pendingDeletePost by mutableStateOf<GuestbookPost?>(null)
        private set

    // ── Edit flow ─────────────────────────────────────────────────────────────
    var editingPost by mutableStateOf<GuestbookPost?>(null)
        private set
    var editMessageInput by mutableStateOf("")
        private set

    /** Existing photo URLs from the original post that the user has NOT removed. */
    var editExistingPhotoUrls by mutableStateOf<List<String>>(emptyList())
        private set

    /** New local photos the user added during this edit session. */
    var editNewPhotoUris by mutableStateOf<List<Uri>>(emptyList())
        private set

    /** True while the edit save + upload is in progress. */
    var editSaving by mutableStateOf(false)
        private set

    val editTotalPhotoCount get() = editExistingPhotoUrls.size + editNewPhotoUris.size

    // ── Permission helpers ────────────────────────────────────────────────────

    fun canEdit(post: GuestbookPost): Boolean {
        val effectiveOwner =
            ContentPermissions.resolveOwnerId(post.ownerIdentityId, post.ownerUserId)
        return ContentPermissions.canEdit(effectiveOwner, currentIdentityId)
    }

    fun canDelete(post: GuestbookPost): Boolean {
        val effectiveOwner =
            ContentPermissions.resolveOwnerId(post.ownerIdentityId, post.ownerUserId)
        return ContentPermissions.canDelete(
            ownerIdentityId = effectiveOwner,
            currentIdentityId = currentIdentityId,
            role = currentGuestRole.value,
            legacyOwnerId = post.guestId,
        )
    }

    fun isOwned(post: GuestbookPost): Boolean {
        val effectiveOwner =
            ContentPermissions.resolveOwnerId(post.ownerIdentityId, post.ownerUserId)
        return effectiveOwner.isNotBlank() && effectiveOwner == currentIdentityId
    }

    // ── New post input ────────────────────────────────────────────────────────

    fun onMessageChange(value: String) {
        messageInput = value
    }

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
                    messageInput = ""; selectedPhotoUris = emptyList(); PostSubmitState.Success
                },
                onFailure = { PostSubmitState.Error(it.message ?: "Failed to post memory") }
            )
        }
    }

    fun resetSubmitState() {
        _submitState.value = PostSubmitState.Idle
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    fun requestDelete(post: GuestbookPost) {
        pendingDeletePost = post
    }

    fun cancelDelete() {
        pendingDeletePost = null
    }

    fun confirmDelete() {
        val post = pendingDeletePost ?: return
        pendingDeletePost = null
        viewModelScope.launch { deleteGuestbookPostUseCase(weddingId, post.id) }
    }

    // ── Edit ──────────────────────────────────────────────────────────────────

    fun openEdit(post: GuestbookPost) {
        editingPost = post
        editMessageInput = post.message
        editExistingPhotoUrls = post.photoUrls
        editNewPhotoUris = emptyList()
    }

    fun onEditMessageChange(value: String) {
        editMessageInput = value
    }

    /** Remove one of the post's original photos from the keep-list. */
    fun removeExistingEditPhoto(url: String) {
        editExistingPhotoUrls = editExistingPhotoUrls - url
    }

    /** Add newly picked local photos (capped at 3 total). */
    fun addEditPhotos(uris: List<Uri>) {
        val remaining = 3 - editTotalPhotoCount
        if (remaining > 0) editNewPhotoUris =
            (editNewPhotoUris + uris).take(editNewPhotoUris.size + remaining)
    }

    /** Remove one of the newly added local photos. */
    fun removeNewEditPhoto(uri: Uri) {
        editNewPhotoUris = editNewPhotoUris - uri
    }

    fun dismissEdit() {
        editingPost = null
        editMessageInput = ""
        editExistingPhotoUrls = emptyList()
        editNewPhotoUris = emptyList()
    }

    fun saveEdit() {
        val post = editingPost ?: return
        if (editMessageInput.isBlank()) return
        val removedUrls = post.photoUrls - editExistingPhotoUrls.toSet()
        val updatedPost = post.copy(
            message = editMessageInput.trim(),
            photoUrls = editExistingPhotoUrls,       // kept URLs; use case appends new ones
            updatedAt = System.currentTimeMillis(),
        )
        val urisToUpload = editNewPhotoUris
        // Clear UI state immediately so sheet can close
        editingPost = null
        editMessageInput = ""
        editExistingPhotoUrls = emptyList()
        editNewPhotoUris = emptyList()
        viewModelScope.launch {
            editSaving = true
            updateGuestbookPostUseCase(
                weddingId,
                updatedPost,
                newPhotoUris = urisToUpload,
                removedPhotoUrls = removedUrls.toList(),
            )
            editSaving = false
        }
    }
}
