package com.wednowapp.wednow.domain.usecase

import com.wednowapp.wednow.domain.repository.GuestGroupRepository
import javax.inject.Inject

class DeleteGuestGroupUseCase @Inject constructor(
    private val repository: GuestGroupRepository,
) {
    suspend operator fun invoke(weddingId: String, groupId: String): Result<Unit> =
        repository.deleteGuestGroup(weddingId, groupId)
}
