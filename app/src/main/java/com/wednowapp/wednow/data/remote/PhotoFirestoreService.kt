package com.wednowapp.wednow.data.remote

import com.google.firebase.firestore.DocumentSnapshot
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

    suspend fun savePhotoMetadata(weddingId: String, photo: WeddingPhoto): Result<Unit> =
        runCatching {
            photosRef(weddingId).document(photo.id).set(photo.toMap()).await()
        }

    private fun WeddingPhoto.toMap(): Map<String, Any> = mapOf(
        "imageUrl" to imageUrl,
        "uploadedBy" to uploadedBy,
        "timestamp" to timestamp
    )

    private fun DocumentSnapshot.toPhoto(): WeddingPhoto? {
        if (!exists()) return null
        return runCatching {
            WeddingPhoto(
                id = id,
                imageUrl = getString("imageUrl") ?: "",
                uploadedBy = getString("uploadedBy") ?: "",
                timestamp = getLong("timestamp") ?: 0L
            )
        }.getOrNull()
    }
}
