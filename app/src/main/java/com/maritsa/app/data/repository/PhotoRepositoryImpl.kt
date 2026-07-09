package com.maritsa.app.data.repository

import android.content.Context
import android.net.Uri
import com.maritsa.app.core.identity.IdentityManager
import com.maritsa.app.core.session.GuestSessionManager
import com.maritsa.app.data.remote.PhotoFirestoreService
import com.maritsa.app.data.remote.PhotoStorageService
import com.maritsa.app.domain.model.WeddingPhoto
import com.maritsa.app.domain.repository.PhotoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepositoryImpl @Inject constructor(
    private val storageService: PhotoStorageService,
    private val firestoreService: PhotoFirestoreService,
    private val identityManager: IdentityManager,
    @ApplicationContext private val context: Context,
) : PhotoRepository {

    override fun getPhotos(weddingId: String): Flow<List<WeddingPhoto>> =
        firestoreService.getPhotos(weddingId)

    override suspend fun uploadPhoto(weddingId: String, uri: Uri): Result<Unit> {
        val photoId = UUID.randomUUID().toString()
        val guestId = GuestSessionManager.getGuestId(context)
        val guestName = GuestSessionManager.getGuestName(context).ifBlank { "A Guest" }
        val identityId = identityManager.currentIdentityId  // UUID (guest) or Firebase UID (user)

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
            ownerUserId = identityId,        // kept for legacy compat
            ownerIdentityId = identityId,        // unified ownership field
        )
        return firestoreService.savePhotoMetadata(weddingId, photo)
    }

    override suspend fun toggleLike(
        weddingId: String,
        photoId: String,
        guestId: String,
        isCurrentlyLiked: Boolean,
    ): Result<Unit> = firestoreService.toggleLike(weddingId, photoId, guestId, isCurrentlyLiked)

    override suspend fun deletePhoto(weddingId: String, photoId: String) =
        firestoreService.deletePhoto(weddingId, photoId)

    override suspend fun updateCaption(weddingId: String, photoId: String, caption: String) =
        firestoreService.updateCaption(weddingId, photoId, caption)
}
