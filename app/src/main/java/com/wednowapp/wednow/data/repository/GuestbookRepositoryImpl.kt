package com.wednowapp.wednow.data.repository

import android.net.Uri
import com.wednowapp.wednow.data.remote.GuestbookFirestoreService
import com.wednowapp.wednow.data.remote.GuestbookStorageService
import com.wednowapp.wednow.domain.model.GuestbookPost
import com.wednowapp.wednow.domain.repository.GuestbookRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuestbookRepositoryImpl @Inject constructor(
    private val firestoreService: GuestbookFirestoreService,
    private val storageService: GuestbookStorageService,
) : GuestbookRepository {

    override fun getPosts(weddingId: String): Flow<List<GuestbookPost>> =
        firestoreService.getPosts(weddingId)

    override suspend fun addPost(weddingId: String, post: GuestbookPost): Result<Unit> =
        firestoreService.addPost(weddingId, post)

    override suspend fun uploadPhotos(
        weddingId: String,
        postId: String,
        uris: List<Uri>,
    ): Result<List<String>> = storageService.uploadPhotos(weddingId, postId, uris)
}
