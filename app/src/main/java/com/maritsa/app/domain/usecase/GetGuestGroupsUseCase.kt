package com.maritsa.app.domain.usecase

import com.maritsa.app.domain.model.GuestGroup
import com.maritsa.app.domain.repository.GuestGroupRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGuestGroupsUseCase @Inject constructor(
    private val repository: GuestGroupRepository,
) {
    operator fun invoke(weddingId: String): Flow<List<GuestGroup>> =
        repository.getGuestGroups(weddingId)
}
