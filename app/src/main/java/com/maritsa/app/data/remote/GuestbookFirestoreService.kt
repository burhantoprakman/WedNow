package com.maritsa.app.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.maritsa.app.domain.model.GuestbookPost
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
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                val posts = snapshot?.documents?.mapNotNull { it.toPost() } ?: emptyList()
                trySend(posts)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addPost(weddingId: String, post: GuestbookPost): Result<Unit> = runCatching {
        postsRef(weddingId).document(post.id).set(post.toMap()).await()
    }

    suspend fun deletePost(weddingId: String, postId: String): Result<Unit> = runCatching {
        postsRef(weddingId).document(postId).delete().await()
    }

    suspend fun updatePost(weddingId: String, post: GuestbookPost): Result<Unit> = runCatching {
        postsRef(weddingId).document(post.id).update(
            mapOf(
                "message" to post.message,
                "photoUrls" to post.photoUrls,
                "updatedAt" to post.updatedAt,
            )
        ).await()
    }

    private fun GuestbookPost.toMap(): Map<String, Any?> = mapOf(
        "guestId" to guestId,
        "senderName" to senderName,
        "message" to message,
        "photoUrls" to photoUrls,
        "timestamp" to timestamp,
        "ownerUserId" to ownerUserId,
        "updatedAt" to updatedAt,
        "ownerIdentityId" to ownerIdentityId,
    )

    private fun DocumentSnapshot.toPost(): GuestbookPost? {
        if (!exists()) return null
        return runCatching {
            GuestbookPost(
                id = id,
                guestId = getString("guestId") ?: "",
                senderName = getString("senderName") ?: "",
                message = getString("message") ?: "",
                photoUrls = (get("photoUrls") as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                timestamp = getLong("timestamp") ?: 0L,
                ownerUserId = getString("ownerUserId") ?: "",
                updatedAt = getLong("updatedAt") ?: 0L,
                // ownerIdentityId: prefer new field, fall back to guestId for legacy posts
                ownerIdentityId = getString("ownerIdentityId")
                    ?.ifBlank { getString("guestId") ?: "" }
                    ?: getString("guestId") ?: "",
            )
        }.getOrNull()
    }
}
