package com.wednowapp.wednow.fake

import android.net.Uri
import com.wednowapp.wednow.domain.model.WeddingPhoto
import com.wednowapp.wednow.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePhotoRepository : PhotoRepository {

    /** weddingId → list of WeddingPhoto */
    private val store = mutableMapOf<String, MutableList<WeddingPhoto>>()
    private val flows = mutableMapOf<String, MutableStateFlow<List<WeddingPhoto>>>()

    var uploadShouldFail = false
    var uploadCallCount = 0

    override fun getPhotos(weddingId: String): Flow<List<WeddingPhoto>> {
        return flows.getOrPut(weddingId) {
            MutableStateFlow(store[weddingId] ?: emptyList())
        }
    }

    override suspend fun uploadPhoto(weddingId: String, uri: Uri): Result<Unit> {
        uploadCallCount++
        if (uploadShouldFail) return Result.failure(RuntimeException("Fake upload failure"))
        // The calling code (PhotoRepositoryImpl) creates the WeddingPhoto — here we just note it was uploaded
        return Result.success(Unit)
    }

    override suspend fun toggleLike(
        weddingId: String,
        photoId: String,
        guestId: String,
        isCurrentlyLiked: Boolean,
    ): Result<Unit> {
        val photos = store[weddingId] ?: return Result.success(Unit)
        val idx = photos.indexOfFirst { it.id == photoId }
        if (idx == -1) return Result.success(Unit)
        val p = photos[idx]
        photos[idx] = if (isCurrentlyLiked) {
            p.copy(likedBy = p.likedBy - guestId, likeCount = p.likeCount - 1)
        } else {
            p.copy(likedBy = p.likedBy + guestId, likeCount = p.likeCount + 1)
        }
        emitUpdate(weddingId)
        return Result.success(Unit)
    }

    override suspend fun deletePhoto(weddingId: String, photoId: String): Result<Unit> {
        store[weddingId]?.removeIf { it.id == photoId }
        emitUpdate(weddingId)
        return Result.success(Unit)
    }

    override suspend fun updateCaption(
        weddingId: String,
        photoId: String,
        caption: String,
    ): Result<Unit> {
        val photos = store[weddingId] ?: return Result.success(Unit)
        val idx = photos.indexOfFirst { it.id == photoId }
        if (idx != -1) photos[idx] = photos[idx].copy(caption = caption)
        emitUpdate(weddingId)
        return Result.success(Unit)
    }

    /** Directly insert a photo (e.g. to simulate pre-existing or migrated data). */
    fun seedPhoto(weddingId: String, photo: WeddingPhoto) {
        store.getOrPut(weddingId) { mutableListOf() }.add(photo)
        emitUpdate(weddingId)
    }

    fun getPhoto(weddingId: String, photoId: String): WeddingPhoto? =
        store[weddingId]?.find { it.id == photoId }

    fun getAllPhotos(weddingId: String): List<WeddingPhoto> = store[weddingId] ?: emptyList()

    private fun emitUpdate(weddingId: String) {
        flows[weddingId]?.value = store[weddingId] ?: emptyList()
    }

    fun reset() {
        store.clear()
        flows.clear()
        uploadShouldFail = false
        uploadCallCount = 0
    }
}
