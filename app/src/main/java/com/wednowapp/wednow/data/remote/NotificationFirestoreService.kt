package com.wednowapp.wednow.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.wednowapp.wednow.domain.model.AppNotification
import com.wednowapp.wednow.domain.model.NotificationType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationFirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun notificationsRef(weddingId: String) =
        firestore.collection("weddings").document(weddingId).collection("notifications")

    fun observeNotifications(weddingId: String): Flow<List<AppNotification>> = callbackFlow {
        val listener = notificationsRef(weddingId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val items = snapshot?.documents?.mapNotNull { it.toNotification() } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendNotification(weddingId: String, notification: AppNotification): Result<Unit> =
        runCatching {
            notificationsRef(weddingId).document(notification.id).set(notification.toMap()).await()
        }

    private fun AppNotification.toMap(): Map<String, Any> = mapOf(
        "title" to title,
        "body" to body,
        "type" to type,
        "timestamp" to timestamp,
        "sentBy" to sentBy,
        "sentByName" to sentByName
    )

    private fun DocumentSnapshot.toNotification(): AppNotification? {
        if (!exists()) return null
        return runCatching {
            AppNotification(
                id = id,
                title = getString("title") ?: "",
                body = getString("body") ?: "",
                type = getString("type") ?: NotificationType.ANNOUNCEMENT,
                timestamp = getLong("timestamp") ?: 0L,
                sentBy = getString("sentBy") ?: "",
                sentByName = getString("sentByName") ?: ""
            )
        }.getOrNull()
    }
}
