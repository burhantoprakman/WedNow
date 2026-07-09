package com.maritsa.app.data.remote

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuestbookStorageService @Inject constructor(
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context,
) {
    /**
     * Uploads [uris] to `weddings/{weddingId}/guestbook/{postId}/photo_{idx}`
     * and returns the list of download URLs in the same order.
     * Reads each URI as bytes on the calling thread (handles Android 13+ scope).
     */
    suspend fun uploadPhotos(
        weddingId: String,
        postId: String,
        uris: List<Uri>,
    ): Result<List<String>> = runCatching {
        uris.mapIndexed { idx, uri ->
            val ref = storage.reference
                .child("weddings/$weddingId/guestbook/$postId/photo_$idx")
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: error("Cannot read photo at index $idx. Please try again.")
            ref.putBytes(bytes).await()
            ref.downloadUrl.await().toString()
        }
    }

    /**
     * Uploads [uris] during an **edit** session using timestamp-based names so
     * they never collide with the original `photo_0 / photo_1 / photo_2` files.
     */
    suspend fun uploadEditPhotos(
        weddingId: String,
        postId: String,
        uris: List<Uri>,
    ): Result<List<String>> = runCatching {
        val ts = System.currentTimeMillis()
        uris.mapIndexed { idx, uri ->
            val ref = storage.reference
                .child("weddings/$weddingId/guestbook/$postId/edit_${ts}_$idx")
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: error("Cannot read edit photo at index $idx. Please try again.")
            ref.putBytes(bytes).await()
            ref.downloadUrl.await().toString()
        }
    }

    /**
     * Deletes the file at the given Firebase Storage download [url].
     * Silently continues if the file no longer exists.
     */
    suspend fun deletePhoto(url: String): Result<Unit> = runCatching {
        storage.getReferenceFromUrl(url).delete().await()
    }
}
