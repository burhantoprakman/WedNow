package com.wednowapp.wednow.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveToken(weddingId: String, guestId: String, token: String): Result<Unit> =
        runCatching {
            firestore.collection("weddings")
                .document(weddingId)
                .collection("guests")
                .document(guestId)
                .update("fcmToken", token)
                .await()
        }
}
