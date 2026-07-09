package com.maritsa.app.domain.usecase

import com.maritsa.app.domain.repository.GuestbookRepository
import javax.inject.Inject

class DeleteGuestbookPostUseCase @Inject constructor(private val repository: GuestbookRepository) {
    suspend operator fun invoke(weddingId: String, postId: String): Result<Unit> =
        repository.deletePost(weddingId, postId)
}
