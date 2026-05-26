package com.wednowapp.wednow.domain.usecase

import com.wednowapp.wednow.domain.model.WeddingPhoto
import com.wednowapp.wednow.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPhotosUseCase @Inject constructor(
    private val repository: PhotoRepository
) {
    operator fun invoke(weddingId: String): Flow<List<WeddingPhoto>> =
        repository.getPhotos(weddingId)
}
