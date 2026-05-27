package com.wednowapp.wednow.domain.repository

import android.net.Uri
import com.wednowapp.wednow.domain.model.WeddingPhoto
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {
    fun getPhotos(weddingId: String): Flow<List<WeddingPhoto>>
    suspend fun uploadPhoto(weddingId: String, uri: Uri): Result<Unit>
    suspend fun toggleLike(
        weddingId: String,
        photoId: String,
        guestId: String,
        isCurrentlyLiked: Boolean,
    ): Result<Unit>
}
