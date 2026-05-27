package com.wednowapp.wednow.data.repository

import com.wednowapp.wednow.data.remote.GuestGroupFirestoreService
import com.wednowapp.wednow.domain.model.GuestGroup
import com.wednowapp.wednow.domain.repository.GuestGroupRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuestGroupRepositoryImpl @Inject constructor(
    private val service: GuestGroupFirestoreService,
) : GuestGroupRepository {
    override fun getGuestGroups(weddingId: String): Flow<List<GuestGroup>> =
        service.getGuestGroups(weddingId)

    override suspend fun addGuestGroup(group: GuestGroup): Result<Unit> =
        service.addGuestGroup(group)

    override suspend fun updateGuestGroup(group: GuestGroup): Result<Unit> =
        service.updateGuestGroup(group)

    override suspend fun deleteGuestGroup(weddingId: String, groupId: String): Result<Unit> =
        service.deleteGuestGroup(weddingId, groupId)
}
