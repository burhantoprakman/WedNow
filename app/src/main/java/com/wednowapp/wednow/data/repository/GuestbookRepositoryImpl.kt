package com.wednowapp.wednow.data.repository

import com.wednowapp.wednow.data.remote.GuestbookFirestoreService
import com.wednowapp.wednow.domain.model.GuestbookPost
import com.wednowapp.wednow.domain.repository.GuestbookRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuestbookRepositoryImpl @Inject constructor(
    private val service: GuestbookFirestoreService
) : GuestbookRepository {

    override fun getPosts(weddingId: String): Flow<List<GuestbookPost>> =
        service.getPosts(weddingId)

    override suspend fun addPost(weddingId: String, post: GuestbookPost): Result<Unit> =
        service.addPost(weddingId, post)
}
