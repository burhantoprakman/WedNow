package com.wednowapp.wednow.fake

import android.net.Uri
import com.wednowapp.wednow.domain.model.GuestbookPost
import com.wednowapp.wednow.domain.repository.GuestbookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeGuestbookRepository : GuestbookRepository {

    /** weddingId → (postId → GuestbookPost) */
    private val store = mutableMapOf<String, MutableMap<String, GuestbookPost>>()
    private val flows = mutableMapOf<String, MutableStateFlow<List<GuestbookPost>>>()

    var uploadedPhotoUrls = listOf("https://storage.fake/photo_0")
    var addShouldFail = false

    override fun getPosts(weddingId: String): Flow<List<GuestbookPost>> {
        val flow = flows.getOrPut(weddingId) {
            MutableStateFlow(store[weddingId]?.values?.sortedByDescending { it.timestamp }
                ?: emptyList())
        }
        return flow
    }

    override suspend fun addPost(weddingId: String, post: GuestbookPost): Result<Unit> {
        if (addShouldFail) return Result.failure(RuntimeException("Fake add failure"))
        store.getOrPut(weddingId) { mutableMapOf() }[post.id] = post
        emitUpdate(weddingId)
        return Result.success(Unit)
    }

    override suspend fun uploadPhotos(
        weddingId: String,
        postId: String,
        uris: List<Uri>,
    ): Result<List<String>> = Result.success(
        uris.mapIndexed { i, _ -> "https://storage.fake/$weddingId/$postId/photo_$i" }
    )

    override suspend fun uploadEditPhotos(
        weddingId: String,
        postId: String,
        uris: List<Uri>,
    ): Result<List<String>> = Result.success(
        uris.mapIndexed { i, _ -> "https://storage.fake/$weddingId/$postId/edit_${System.currentTimeMillis()}_$i" }
    )

    override suspend fun deletePhoto(url: String): Result<Unit> = Result.success(Unit)

    override suspend fun deletePost(weddingId: String, postId: String): Result<Unit> {
        store[weddingId]?.remove(postId)
        emitUpdate(weddingId)
        return Result.success(Unit)
    }

    override suspend fun updatePost(weddingId: String, post: GuestbookPost): Result<Unit> {
        store[weddingId]?.set(post.id, post)
        emitUpdate(weddingId)
        return Result.success(Unit)
    }

    fun getPost(weddingId: String, postId: String): GuestbookPost? = store[weddingId]?.get(postId)

    fun getAllPosts(weddingId: String): List<GuestbookPost> =
        store[weddingId]?.values?.toList() ?: emptyList()

    /** Directly insert a post (e.g. legacy data with empty ownerIdentityId). */
    fun seedPost(weddingId: String, post: GuestbookPost) {
        store.getOrPut(weddingId) { mutableMapOf() }[post.id] = post
        emitUpdate(weddingId)
    }

    private fun emitUpdate(weddingId: String) {
        flows[weddingId]?.value =
            store[weddingId]?.values?.sortedByDescending { it.timestamp } ?: emptyList()
    }

    fun reset() {
        store.clear()
        flows.clear()
        addShouldFail = false
    }
}
