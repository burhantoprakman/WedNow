package com.wednowapp.wednow.domain.repository

import com.wednowapp.wednow.domain.model.GuestbookPost
import kotlinx.coroutines.flow.Flow

interface GuestbookRepository {
    fun getPosts(weddingId: String): Flow<List<GuestbookPost>>
    suspend fun addPost(weddingId: String, post: GuestbookPost): Result<Unit>
}
