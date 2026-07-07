package com.wednowapp.wednow.presentation.photos

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wednowapp.wednow.core.identity.IdentityManager
import com.wednowapp.wednow.core.navigation.Screen
import com.wednowapp.wednow.core.session.GuestSessionManager
import com.wednowapp.wednow.domain.model.ContentPermissions
import com.wednowapp.wednow.domain.model.WeddingPhoto
import com.wednowapp.wednow.domain.usecase.DeletePhotoUseCase
import com.wednowapp.wednow.domain.usecase.GetCurrentGuestUseCase
import com.wednowapp.wednow.domain.usecase.GetPhotosUseCase
import com.wednowapp.wednow.domain.usecase.SendPhotoLikeNotificationUseCase
import com.wednowapp.wednow.domain.usecase.ToggleLikeUseCase
import com.wednowapp.wednow.domain.usecase.UpdatePhotoCaptionUseCase
import com.wednowapp.wednow.domain.usecase.UploadPhotoUseCase
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

sealed class UploadState {
    object Idle : UploadState()
    data class Loading(val current: Int = 1, val total: Int = 1) : UploadState()
    data class Success(val count: Int = 1) : UploadState()
    data class Error(val message: String) : UploadState()
}

@HiltViewModel
class PhotosViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getPhotosUseCase: GetPhotosUseCase,
    getCurrentGuestUseCase: GetCurrentGuestUseCase,
    private val uploadPhotoUseCase: UploadPhotoUseCase,
    private val toggleLikeUseCase: ToggleLikeUseCase,
    private val deletePhotoUseCase: DeletePhotoUseCase,
    private val updatePhotoCaptionUseCase: UpdatePhotoCaptionUseCase,
    private val sendPhotoLikeNotification: SendPhotoLikeNotificationUseCase,
    private val identityManager: IdentityManager,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val weddingId: String = checkNotNull(savedStateHandle[Screen.Photos.ARG])

    /** Legacy guestId UUID — used for like operations and as a legacy ownership fallback. */
    val currentGuestId: String = GuestSessionManager.getGuestId(context)

    /**
     * Unified identity ID — UUID for anonymous guests, Firebase UID for signed-in users.
     * Use this for all ownership and permission checks instead of [currentGuestId].
     */
    val currentIdentityId: String get() = identityManager.currentIdentityId

    val photos: StateFlow<List<WeddingPhoto>?> = getPhotosUseCase(weddingId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = null)

    /** Current guest's role — drives delete permission for admins/co-admins. */
    val currentGuestRole: StateFlow<String?> = getCurrentGuestUseCase(weddingId)
        .map { it?.role }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = null)

    /**
     * The photo shown in the "Featured Memory" hero slot.
     * Picks the most-liked photo; if all like counts are zero, falls back to the most recent.
     */
    val featuredPhoto: StateFlow<WeddingPhoto?> = photos
        .map { list ->
            if (list.isNullOrEmpty()) null
            else if (list.all { it.likedBy.isEmpty() })
                list.maxByOrNull { it.timestamp }
            else
                list.maxByOrNull { it.likedBy.size }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = null)

    /**
     * Photos uploaded by the current identity.
     * Works for both authenticated users and anonymous guests.
     */
    val myPhotos: StateFlow<List<WeddingPhoto>> = photos
        .map { list ->
            if (list == null) emptyList()
            else {
                val id = identityManager.currentIdentityId
                list.filter { photo ->
                    ContentPermissions.resolveOwnerId(
                        photo.ownerIdentityId,
                        photo.ownerUserId
                    ) == id ||
                            photo.uploadedBy == currentGuestId
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = emptyList())

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    // ── Delete flow ───────────────────────────────────────────────────────────

    /** Non-null while the delete confirmation dialog is showing. */
    var pendingDeletePhoto by mutableStateOf<WeddingPhoto?>(null)
        private set

    private val _deleteState = MutableStateFlow<Result<Unit>?>(null)
    val deleteState: StateFlow<Result<Unit>?> = _deleteState.asStateFlow()

    // ── Edit caption flow ─────────────────────────────────────────────────────

    var editCaptionTarget by mutableStateOf<WeddingPhoto?>(null)
        private set

    // ── Permission helpers ────────────────────────────────────────────────────

    fun canEdit(photo: WeddingPhoto): Boolean {
        val effectiveOwner =
            ContentPermissions.resolveOwnerId(photo.ownerIdentityId, photo.ownerUserId)
        return ContentPermissions.canEdit(effectiveOwner, currentIdentityId) ||
                photo.uploadedBy == currentGuestId   // legacy fallback
    }

    fun canDelete(photo: WeddingPhoto): Boolean {
        val effectiveOwner =
            ContentPermissions.resolveOwnerId(photo.ownerIdentityId, photo.ownerUserId)
        return ContentPermissions.canDelete(
            ownerIdentityId = effectiveOwner,
            currentIdentityId = currentIdentityId,
            role = currentGuestRole.value,
            legacyOwnerId = photo.uploadedBy,
        )
    }

    fun isOwned(photo: WeddingPhoto): Boolean {
        val effectiveOwner =
            ContentPermissions.resolveOwnerId(photo.ownerIdentityId, photo.ownerUserId)
        return effectiveOwner.isNotBlank() && effectiveOwner == currentIdentityId
    }

    // ── Upload ────────────────────────────────────────────────────────────────

    fun uploadMultiple(uris: List<Uri>) {
        if (_uploadState.value is UploadState.Loading) return
        val batch = uris.take(10)
        viewModelScope.launch {
            var successCount = 0
            for ((index, uri) in batch.withIndex()) {
                _uploadState.value = UploadState.Loading(current = index + 1, total = batch.size)
                val result = uploadPhotoUseCase(weddingId, uri)
                if (result.isSuccess) successCount++
                else {
                    _uploadState.value =
                        UploadState.Error(result.exceptionOrNull()?.message ?: "Upload failed")
                    return@launch
                }
            }
            _uploadState.value = UploadState.Success(count = successCount)
        }
    }

    fun resetUploadState() {
        _uploadState.value = UploadState.Idle
    }

    // ── Like ──────────────────────────────────────────────────────────────────

    fun toggleLike(photoId: String, isCurrentlyLiked: Boolean) {
        viewModelScope.launch {
            toggleLikeUseCase(weddingId, photoId, currentGuestId, isCurrentlyLiked)
            // Only send notification on a NEW like (not on unlike)
            if (!isCurrentlyLiked) {
                val ownerIdentityId = photos.value
                    ?.find { it.id == photoId }
                    ?.let { ContentPermissions.resolveOwnerId(it.ownerIdentityId, it.ownerUserId) }
                    .orEmpty()
                sendPhotoLikeNotification(weddingId, photoId, ownerIdentityId)
            }
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    fun requestDelete(photo: WeddingPhoto) {
        pendingDeletePhoto = photo
    }

    fun cancelDelete() {
        pendingDeletePhoto = null
    }

    fun confirmDelete() {
        val photo = pendingDeletePhoto ?: return
        pendingDeletePhoto = null
        viewModelScope.launch {
            _deleteState.value = deletePhotoUseCase(weddingId, photo.id)
        }
    }

    fun clearDeleteState() {
        _deleteState.value = null
    }

    // ── Edit caption ──────────────────────────────────────────────────────────

    fun openEditCaption(photo: WeddingPhoto) {
        editCaptionTarget = photo
    }

    fun dismissEditCaption() {
        editCaptionTarget = null
    }

    fun saveCaption(caption: String) {
        val photo = editCaptionTarget ?: return
        editCaptionTarget = null
        viewModelScope.launch {
            updatePhotoCaptionUseCase(weddingId, photo.id, caption.trim())
        }
    }
}
