package com.maritsa.app.data.repository

import android.net.Uri
import com.maritsa.app.data.remote.GuestbookFirestoreService
import com.maritsa.app.data.remote.GuestbookStorageService
import com.maritsa.app.domain.model.GuestbookPost
import com.maritsa.app.domain.repository.GuestbookRepository
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

    override suspend fun uploadEditPhotos(
        weddingId: String,
        postId: String,
        uris: List<Uri>,
    ): Result<List<String>> = storageService.uploadEditPhotos(weddingId, postId, uris)

    override suspend fun deletePhoto(url: String): Result<Unit> =
        storageService.deletePhoto(url)

    override suspend fun deletePost(weddingId: String, postId: String) =
        firestoreService.deletePost(weddingId, postId)

    override suspend fun updatePost(weddingId: String, post: GuestbookPost) =
        firestoreService.updatePost(weddingId, post)
}
