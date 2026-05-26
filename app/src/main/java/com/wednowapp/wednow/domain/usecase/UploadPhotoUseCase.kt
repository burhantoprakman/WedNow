package com.wednowapp.wednow.domain.usecase

import android.net.Uri
import com.wednowapp.wednow.domain.repository.PhotoRepository
import javax.inject.Inject

class UploadPhotoUseCase @Inject constructor(
    private val repository: PhotoRepository
) {
    suspend operator fun invoke(weddingId: String, uri: Uri): Result<Unit> =
        repository.uploadPhoto(weddingId, uri)
}
