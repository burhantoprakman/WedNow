package com.maritsa.app.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.maritsa.app.domain.model.ChatMessage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatFirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun chatRef(weddingId: String) =
        firestore.collection("weddings").document(weddingId).collection("chat")

    fun observeMessages(weddingId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = chatRef(weddingId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { it.toChatMessage() } ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(weddingId: String, message: ChatMessage): Result<Unit> = runCatching {
        chatRef(weddingId).document(message.id).set(message.toMap()).await()
    }

    private fun ChatMessage.toMap(): Map<String, Any> = mapOf(
        "message" to message,
        "guestId" to guestId,
        "guestName" to guestName,
        "timestamp" to timestamp
    )

    private fun DocumentSnapshot.toChatMessage(): ChatMessage? {
        if (!exists()) return null
        return runCatching {
            ChatMessage(
                id = id,
                message = getString("message") ?: "",
                guestId = getString("guestId") ?: "",
                guestName = getString("guestName") ?: "",
                timestamp = getLong("timestamp") ?: 0L
            )
        }.getOrNull()
    }
}
