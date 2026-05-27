package com.wednowapp.wednow.presentation.photos

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wednowapp.wednow.core.navigation.Screen
import com.wednowapp.wednow.core.session.GuestSessionManager
import com.wednowapp.wednow.domain.model.WeddingPhoto
import com.wednowapp.wednow.domain.usecase.GetPhotosUseCase
import com.wednowapp.wednow.domain.usecase.ToggleLikeUseCase
import com.wednowapp.wednow.domain.usecase.UploadPhotoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UploadState {
    object Idle : UploadState()

    /** [current] = 1-based index of the photo being uploaded right now. [total] = batch size. */
    data class Loading(val current: Int = 1, val total: Int = 1) : UploadState()

    /** [count] = number of photos successfully uploaded in this batch. */
    data class Success(val count: Int = 1) : UploadState()
    data class Error(val message: String) : UploadState()
}

@HiltViewModel
class PhotosViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getPhotosUseCase: GetPhotosUseCase,
    private val uploadPhotoUseCase: UploadPhotoUseCase,
    private val toggleLikeUseCase: ToggleLikeUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val weddingId: String = checkNotNull(savedStateHandle[Screen.Photos.ARG])

    /** Stable across the session — used for isLiked checks in the UI. */
    val currentGuestId: String = GuestSessionManager.getGuestId(context)

    /**
     * Null = first Firestore emission not yet received (loading).
     * Empty list = loaded but no photos yet.
     */
    val photos: StateFlow<List<WeddingPhoto>?> = getPhotosUseCase(weddingId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = null)

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    /**
     * Uploads [uris] one at a time (max 10), keeping the UI informed of progress.
     * Stops on the first failure and reports the error message.
     */
    fun uploadMultiple(uris: List<Uri>) {
        if (_uploadState.value is UploadState.Loading) return
        val batch = uris.take(10)          // hard cap — mirrors the picker limit
        viewModelScope.launch {
            var successCount = 0
            for ((index, uri) in batch.withIndex()) {
                _uploadState.value = UploadState.Loading(current = index + 1, total = batch.size)
                val result = uploadPhotoUseCase(weddingId, uri)
                if (result.isSuccess) {
                    successCount++
                } else {
                    _uploadState.value = UploadState.Error(
                        result.exceptionOrNull()?.message ?: "Upload failed"
                    )
                    return@launch
                }
            }
            _uploadState.value = UploadState.Success(count = successCount)
        }
    }

    fun resetUploadState() {
        _uploadState.value = UploadState.Idle
    }

    /**
     * Toggles the like state for the current guest on [photoId].
     * The Firestore realtime listener in [getPhotosUseCase] will pick up the
     * change and update [photos] automatically — no manual state needed here.
     */
    fun toggleLike(photoId: String, isCurrentlyLiked: Boolean) {
        viewModelScope.launch {
            toggleLikeUseCase(weddingId, photoId, currentGuestId, isCurrentlyLiked)
        }
    }
}
