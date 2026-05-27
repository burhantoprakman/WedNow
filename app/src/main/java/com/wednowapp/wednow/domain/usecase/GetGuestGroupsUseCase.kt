package com.wednowapp.wednow.domain.usecase

import com.wednowapp.wednow.domain.model.GuestGroup
import com.wednowapp.wednow.domain.repository.GuestGroupRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGuestGroupsUseCase @Inject constructor(
    private val repository: GuestGroupRepository,
) {
    operator fun invoke(weddingId: String): Flow<List<GuestGroup>> =
        repository.getGuestGroups(weddingId)
}
