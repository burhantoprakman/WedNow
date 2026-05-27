package com.wednowapp.wednow.data.remote

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoStorageService @Inject constructor(
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context,
) {
    /**
     * Uploads a photo to Firebase Storage and returns its public download URL.
     *
     * Uses [putBytes] (reads the file into memory first) instead of [putFile]
     * so the content URI is consumed on the calling thread while the app still
     * holds the picker permission grant — avoids "permission denied" edge cases
     * on Android 13+ where the grant can narrow across thread boundaries.
     *
     * Common failure: if this returns "Object does not exist", your Firebase
     * Storage Security Rules are blocking getDownloadUrl().  Open
     * Firebase Console → Storage → Rules and add:
     *   allow read: if true;
     *   allow write: if true;
     * under the relevant path.
     */
    suspend fun uploadPhoto(weddingId: String, photoId: String, uri: Uri): Result<String> =
        runCatching {
            val ref = storage.reference.child("weddings/$weddingId/photos/$photoId")

            // Read bytes on this coroutine dispatcher (still holds URI permission)
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: error("Cannot read the selected photo. Please try again.")

            ref.putBytes(bytes).await()

            ref.downloadUrl.await().toString()
        }.mapFailure()

    // ── helpers ───────────────────────────────────────────────────────────────

    /**
     * Translates raw Firebase [StorageException] codes into human-readable messages
     * so they can be displayed directly in the UI without leaking internal paths.
     */
    private fun <T> Result<T>.mapFailure(): Result<T> =
        onFailure { throwable ->
            // Re-throw with a cleaner message; the original cause is preserved.
            val friendly = when {
                throwable is StorageException &&
                        throwable.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND ->
                    "Upload failed: Storage access is restricted. " +
                            "Ask the wedding organiser to check Firebase Storage rules."

                throwable is StorageException &&
                        throwable.errorCode == StorageException.ERROR_NOT_AUTHORIZED ->
                    "Upload failed: You don't have permission to upload photos."

                throwable is StorageException &&
                        throwable.errorCode == StorageException.ERROR_QUOTA_EXCEEDED ->
                    "Upload failed: Storage quota exceeded."

                throwable.message?.contains("Cannot read", ignoreCase = true) == true ->
                    throwable.message!!   // our own message — pass through as-is

                else ->
                    "Upload failed. Please check your connection and try again."
            }
            // Wrap in a plain exception so the message reaches the ViewModel cleanly
            return Result.failure(Exception(friendly, throwable))
        }
}
