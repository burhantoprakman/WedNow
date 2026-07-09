package com.maritsa.app.domain.repository

import android.net.Uri
import com.maritsa.app.domain.model.GuestbookPost
import kotlinx.coroutines.flow.Flow

interface GuestbookRepository {
    fun getPosts(weddingId: String): Flow<List<GuestbookPost>>
    suspend fun addPost(weddingId: String, post: GuestbookPost): Result<Unit>
    suspend fun uploadPhotos(
        weddingId: String,
        postId: String,
        uris: List<Uri>,
    ): Result<List<String>>

    /** Upload photos added during an edit — uses timestamp-safe file names. */
    suspend fun uploadEditPhotos(
        weddingId: String,
        postId: String,
        uris: List<Uri>,
    ): Result<List<String>>

    /** Delete a single photo file from Storage by its download URL. Best-effort. */
    suspend fun deletePhoto(url: String): Result<Unit>
    suspend fun deletePost(weddingId: String, postId: String): Result<Unit>
    suspend fun updatePost(weddingId: String, post: GuestbookPost): Result<Unit>
}
