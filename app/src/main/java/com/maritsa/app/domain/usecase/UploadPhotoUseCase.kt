package com.maritsa.app.domain.usecase

import android.net.Uri
import com.maritsa.app.domain.repository.PhotoRepository
import javax.inject.Inject

class UploadPhotoUseCase @Inject constructor(
    private val repository: PhotoRepository
) {
    suspend operator fun invoke(weddingId: String, uri: Uri): Result<Unit> =
        repository.uploadPhoto(weddingId, uri)
}
