package com.wednowapp.wednow.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.wednowapp.wednow.domain.model.GuestbookPost
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuestbookFirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun postsRef(weddingId: String) =
        firestore.collection("weddings").document(weddingId).collection("guestbook")

    fun getPosts(weddingId: String): Flow<List<GuestbookPost>> = callbackFlow {
        val listener = postsRef(weddingId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val posts = snapshot?.documents?.mapNotNull { it.toPost() } ?: emptyList()
                trySend(posts)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addPost(weddingId: String, post: GuestbookPost): Result<Unit> = runCatching {
        postsRef(weddingId).document(post.id).set(post.toMap()).await()
    }

    private fun GuestbookPost.toMap(): Map<String, Any> = mapOf(
        "guestId" to guestId,
        "message" to message,
        "timestamp" to timestamp
    )

    private fun DocumentSnapshot.toPost(): GuestbookPost? {
        if (!exists()) return null
        return runCatching {
            GuestbookPost(
                id = id,
                guestId = getString("guestId") ?: "",
                message = getString("message") ?: "",
                timestamp = getLong("timestamp") ?: 0L
            )
        }.getOrNull()
    }
}
