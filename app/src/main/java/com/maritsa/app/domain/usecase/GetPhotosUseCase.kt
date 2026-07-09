package com.maritsa.app.domain.usecase

import com.maritsa.app.domain.model.WeddingPhoto
import com.maritsa.app.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPhotosUseCase @Inject constructor(
    private val repository: PhotoRepository
) {
    operator fun invoke(weddingId: String): Flow<List<WeddingPhoto>> =
        repository.getPhotos(weddingId)
}
