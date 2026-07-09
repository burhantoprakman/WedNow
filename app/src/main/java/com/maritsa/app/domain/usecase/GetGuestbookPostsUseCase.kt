package com.maritsa.app.domain.usecase

import com.maritsa.app.domain.model.GuestbookPost
import com.maritsa.app.domain.repository.GuestbookRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGuestbookPostsUseCase @Inject constructor(
    private val repository: GuestbookRepository
) {
    operator fun invoke(weddingId: String): Flow<List<GuestbookPost>> =
        repository.getPosts(weddingId)
}
