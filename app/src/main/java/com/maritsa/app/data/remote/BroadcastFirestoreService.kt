package com.maritsa.app.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.maritsa.app.domain.model.Broadcast
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BroadcastFirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun broadcastsRef(weddingId: String) =
        firestore.collection("weddings").document(weddingId).collection("broadcasts")

    fun observeBroadcasts(weddingId: String): Flow<List<Broadcast>> = callbackFlow {
        val listener = broadcastsRef(weddingId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { it.toBroadcast() } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendBroadcast(weddingId: String, broadcast: Broadcast): Result<Unit> = runCatching {
        broadcastsRef(weddingId).document(broadcast.id).set(broadcast.toMap()).await()
    }

    private fun Broadcast.toMap(): Map<String, Any> = mapOf(
        "message" to message,
        "sentBy" to sentBy,
        "sentByName" to sentByName,
        "timestamp" to timestamp
    )

    private fun DocumentSnapshot.toBroadcast(): Broadcast? {
        if (!exists()) return null
        return runCatching {
            Broadcast(
                id = id,
                message = getString("message") ?: "",
                sentBy = getString("sentBy") ?: "",
                sentByName = getString("sentByName") ?: "",
                timestamp = getLong("timestamp") ?: 0L
            )
        }.getOrNull()
    }
}
