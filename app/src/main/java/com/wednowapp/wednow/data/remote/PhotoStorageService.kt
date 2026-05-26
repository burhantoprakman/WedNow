package com.wednowapp.wednow.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoStorageService @Inject constructor(
    private val storage: FirebaseStorage
) {
    suspend fun uploadPhoto(weddingId: String, photoId: String, uri: Uri): Result<String> =
        runCatching {
            val ref = storage.reference.child("weddings/$weddingId/photos/$photoId")
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        }
}
