package com.wednowapp.wednow.presentation.photos

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wednowapp.wednow.core.navigation.Screen
import com.wednowapp.wednow.domain.model.WeddingPhoto
import com.wednowapp.wednow.domain.usecase.GetPhotosUseCase
import com.wednowapp.wednow.domain.usecase.UploadPhotoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    object Success : UploadState()
    data class Error(val message: String) : UploadState()
}

@HiltViewModel
class PhotosViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getPhotosUseCase: GetPhotosUseCase,
    private val uploadPhotoUseCase: UploadPhotoUseCase
) : ViewModel() {

    val weddingId: String = checkNotNull(savedStateHandle[Screen.Photos.ARG])

    val photos: StateFlow<List<WeddingPhoto>> = getPhotosUseCase(weddingId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    fun upload(uri: Uri) {
        if (_uploadState.value == UploadState.Loading) return
        viewModelScope.launch {
            _uploadState.value = UploadState.Loading
            val result = uploadPhotoUseCase(weddingId, uri)
            _uploadState.value = result.fold(
                onSuccess = { UploadState.Success },
                onFailure = { UploadState.Error(it.message ?: "Upload failed") }
            )
        }
    }

    fun resetUploadState() {
        _uploadState.value = UploadState.Idle
    }
}
