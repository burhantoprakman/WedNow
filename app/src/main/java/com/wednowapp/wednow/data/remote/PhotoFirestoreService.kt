package com.wednowapp.wednow.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.wednowapp.wednow.domain.model.WeddingPhoto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoFirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun photosRef(weddingId: String) =
        firestore.collection("weddings").document(weddingId).collection("photos")

    // ── Read ──────────────────────────────────────────────────────────────────

    fun getPhotos(weddingId: String): Flow<List<WeddingPhoto>> = callbackFlow {
        val listener = photosRef(weddingId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val photos = snapshot?.documents?.mapNotNull { it.toPhoto() } ?: emptyList()
                trySend(photos)
            }
        awaitClose { listener.remove() }
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    suspend fun savePhotoMetadata(weddingId: String, photo: WeddingPhoto): Result<Unit> =
        runCatching {
            photosRef(weddingId).document(photo.id).set(photo.toMap()).await()
        }

    /**
     * Atomically toggles a like on a photo.
     *
     * [isCurrentlyLiked] = true  → unlike  (arrayRemove + decrement)
     * [isCurrentlyLiked] = false → like    (arrayUnion  + increment)
     *
     * Because Firestore processes these as server-side operations there are
     * no race conditions even with simultaneous guests liking the same photo.
     */
    suspend fun toggleLike(
        weddingId: String,
        photoId: String,
        guestId: String,
        isCurrentlyLiked: Boolean,
    ): Result<Unit> = runCatching {
        val ref = photosRef(weddingId).document(photoId)
        if (isCurrentlyLiked) {
            ref.update(
                "likedBy", FieldValue.arrayRemove(guestId),
                "likeCount", FieldValue.increment(-1),
            ).await()
        } else {
            ref.update(
                "likedBy", FieldValue.arrayUnion(guestId),
                "likeCount", FieldValue.increment(1),
            ).await()
        }
    }

    // ── Serialization ─────────────────────────────────────────────────────────

    private fun WeddingPhoto.toMap(): Map<String, Any> = buildMap {
        put("imageUrl", imageUrl)
        put("uploadedBy", uploadedBy)
        put("senderName", senderName)
        put("timestamp", timestamp)
        put("likeCount", likeCount)
        put("likedBy", likedBy)
    }

    private fun DocumentSnapshot.toPhoto(): WeddingPhoto? {
        if (!exists()) return null
        return runCatching {
            WeddingPhoto(
                id = id,
                imageUrl = getString("imageUrl") ?: "",
                uploadedBy = getString("uploadedBy") ?: "",
                senderName = getString("senderName") ?: "",
                timestamp = getLong("timestamp") ?: 0L,
                likeCount = getLong("likeCount")?.toInt() ?: 0,
                // Firestore returns List<Any> — safely cast to List<String>
                likedBy = (get("likedBy") as? List<*>)
                    ?.filterIsInstance<String>()
                    ?: emptyList(),
            )
        }.getOrNull()
    }
}
