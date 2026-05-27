package com.wednowapp.wednow.data.repository

import android.content.Context
import android.net.Uri
import com.wednowapp.wednow.core.session.GuestSessionManager
import com.wednowapp.wednow.data.remote.PhotoFirestoreService
import com.wednowapp.wednow.data.remote.PhotoStorageService
import com.wednowapp.wednow.domain.model.WeddingPhoto
import com.wednowapp.wednow.domain.repository.PhotoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepositoryImpl @Inject constructor(
    private val storageService: PhotoStorageService,
    private val firestoreService: PhotoFirestoreService,
    @ApplicationContext private val context: Context
) : PhotoRepository {

    override fun getPhotos(weddingId: String): Flow<List<WeddingPhoto>> =
        firestoreService.getPhotos(weddingId)

    override suspend fun uploadPhoto(weddingId: String, uri: Uri): Result<Unit> {
        val photoId = UUID.randomUUID().toString()
        val guestId = GuestSessionManager.getGuestId(context)
        val guestName = GuestSessionManager.getGuestName(context)
            .ifBlank { "A Guest" }

        val imageUrl = storageService.uploadPhoto(weddingId, photoId, uri)
            .getOrElse { return Result.failure(it) }

        val photo = WeddingPhoto(
            id = photoId,
            imageUrl = imageUrl,
            uploadedBy = guestId,
            senderName = guestName,
            timestamp = System.currentTimeMillis(),
            likeCount = 0,
            likedBy = emptyList(),
        )
        return firestoreService.savePhotoMetadata(weddingId, photo)
    }

    override suspend fun toggleLike(
        weddingId: String,
        photoId: String,
        guestId: String,
        isCurrentlyLiked: Boolean,
    ): Result<Unit> = firestoreService.toggleLike(weddingId, photoId, guestId, isCurrentlyLiked)
}
