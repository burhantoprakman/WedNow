package com.wednowapp.wednow.domain.usecase

import com.wednowapp.wednow.domain.model.GuestbookPost
import com.wednowapp.wednow.domain.repository.GuestbookRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGuestbookPostsUseCase @Inject constructor(
    private val repository: GuestbookRepository
) {
    operator fun invoke(weddingId: String): Flow<List<GuestbookPost>> =
        repository.getPosts(weddingId)
}
