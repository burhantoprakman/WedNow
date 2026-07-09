package com.maritsa.app.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.maritsa.app.domain.model.AppNotification
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Low-level Firestore access for wedding notifications.
 *
 * Collection layout:
 *   weddings/{weddingId}/notifications/{notificationId}
 *   weddings/{weddingId}/notificationReads/{identityId}  ← per-user read set
 *
 * Push delivery is handled server-side by Cloud Functions that listen for
 * onCreate events in the notifications collection.
 */
@Singleton
class NotificationFirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore,
) {

    // ── Collection helpers ────────────────────────────────────────────────────

    private fun notificationsRef(weddingId: String) =
        firestore.collection("weddings").document(weddingId).collection("notifications")

    private fun readsRef(weddingId: String) =
        firestore.collection("weddings").document(weddingId).collection("notificationReads")

    // ── Observe ───────────────────────────────────────────────────────────────

    /**
     * Streams all notifications for [weddingId] that are either broadcast
     * (recipientId == "") or addressed to [recipientId], newest first.
     *
     * Note: Firestore does not support OR queries across different field values
     * in a single query, so we fetch the last 200 docs and filter in memory.
     * For typical wedding sizes (20–300 guests, <2 000 total notifications)
     * this is well within free-tier limits.
     */
    fun observeNotifications(
        weddingId: String,
        recipientId: String,
    ): Flow<List<AppNotification>> = callbackFlow {
        val listener = notificationsRef(weddingId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(200)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                val all = snapshot?.documents?.mapNotNull { it.toNotification() } ?: emptyList()
                val filtered = all.filter { n ->
                    n.recipientId.isBlank() || n.recipientId == recipientId
                }
                trySend(filtered)
            }
        awaitClose { listener.remove() }
    }

    /** Observe the cross-device read set for [identityId] as a live Flow<Set<String>>. */
    fun observeReadIds(weddingId: String, identityId: String): Flow<Set<String>> = callbackFlow {
        val listener = readsRef(weddingId).document(identityId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                @Suppress("UNCHECKED_CAST")
                val ids = (snapshot?.get("ids") as? List<String>)?.toSet() ?: emptySet()
                trySend(ids)
            }
        awaitClose { listener.remove() }
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    /** Persist a new notification document. Cloud Function picks this up for FCM delivery. */
    suspend fun sendNotification(
        weddingId: String,
        notification: AppNotification,
    ): Result<Unit> = runCatching {
        notificationsRef(weddingId)
            .document(notification.id)
            .set(notification.toMap())
            .await()
    }

    /** Append [notificationId] to this identity's Firestore read set (array-union). */
    suspend fun markAsRead(
        weddingId: String,
        notificationId: String,
        identityId: String,
    ): Result<Unit> = runCatching {
        readsRef(weddingId).document(identityId)
            .set(
                mapOf("ids" to FieldValue.arrayUnion(notificationId)),
                com.google.firebase.firestore.SetOptions.merge()
            )
            .await()
    }

    /** Bulk mark-all-read using a batch array-union. */
    suspend fun markAllAsRead(
        weddingId: String,
        notificationIds: List<String>,
        identityId: String,
    ): Result<Unit> = runCatching {
        if (notificationIds.isEmpty()) return@runCatching
        readsRef(weddingId).document(identityId)
            .set(
                mapOf("ids" to FieldValue.arrayUnion(*notificationIds.toTypedArray())),
                com.google.firebase.firestore.SetOptions.merge()
            )
            .await()
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private fun AppNotification.toMap(): Map<String, Any> = buildMap {
        put("id", id)
        put("weddingId", weddingId)
        put("recipientId", recipientId)
        put("senderId", senderId)
        put("senderName", senderName)
        put("type", type)
        put("title", title)
        put("body", body)
        put("targetId", targetId)
        put("targetScreen", targetScreen)
        put("createdAt", createdAt)
        put("imageUrl", imageUrl)
        // Cloud Function sets fcmSent = true after delivery; initialize to false
        put("fcmSent", false)
    }

    private fun DocumentSnapshot.toNotification(): AppNotification? {
        if (!exists()) return null
        return runCatching {
            AppNotification(
                id = id,
                weddingId = getString("weddingId") ?: "",
                recipientId = getString("recipientId") ?: "",
                senderId = getString("senderId") ?: "",
                senderName = getString("senderName") ?: "",
                type = getString("type") ?: "",
                title = getString("title") ?: "",
                body = getString("body") ?: "",
                targetId = getString("targetId") ?: "",
                targetScreen = getString("targetScreen") ?: "",
                createdAt = getLong("createdAt") ?: 0L,
                imageUrl = getString("imageUrl") ?: "",
            )
        }.getOrNull()
    }
}
