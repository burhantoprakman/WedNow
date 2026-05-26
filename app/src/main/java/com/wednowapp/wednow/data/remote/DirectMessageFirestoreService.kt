package com.wednowapp.wednow.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.wednowapp.wednow.domain.model.DirectMessage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DirectMessageFirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun messagesRef(weddingId: String, channelId: String) =
        firestore.collection("weddings").document(weddingId)
            .collection("dm").document(channelId)
            .collection("messages")

    fun observeMessages(weddingId: String, channelId: String): Flow<List<DirectMessage>> =
        callbackFlow {
            val listener = messagesRef(weddingId, channelId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) { close(error); return@addSnapshotListener }
                    val messages = snapshot?.documents?.mapNotNull { it.toDirectMessage() }
                        ?: emptyList()
                    trySend(messages)
                }
            awaitClose { listener.remove() }
        }

    suspend fun sendMessage(
        weddingId: String,
        channelId: String,
        message: DirectMessage
    ): Result<Unit> = runCatching {
        messagesRef(weddingId, channelId).document(message.id).set(message.toMap()).await()
    }

    private fun DirectMessage.toMap(): Map<String, Any> = mapOf(
        "message" to message,
        "senderId" to senderId,
        "senderName" to senderName,
        "timestamp" to timestamp
    )

    private fun DocumentSnapshot.toDirectMessage(): DirectMessage? {
        if (!exists()) return null
        return runCatching {
            DirectMessage(
                id = id,
                message = getString("message") ?: "",
                senderId = getString("senderId") ?: "",
                senderName = getString("senderName") ?: "",
                timestamp = getLong("timestamp") ?: 0L
            )
        }.getOrNull()
    }
}
