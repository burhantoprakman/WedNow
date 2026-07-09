package com.maritsa.app.data.repository

import com.maritsa.app.data.remote.GuestGroupFirestoreService
import com.maritsa.app.domain.model.GuestGroup
import com.maritsa.app.domain.model.GuestMember
import com.maritsa.app.domain.repository.GuestGroupRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuestGroupRepositoryImpl @Inject constructor(
    private val service: GuestGroupFirestoreService,
) : GuestGroupRepository {

    override fun getGuestGroups(weddingId: String): Flow<List<GuestGroup>> =
        service.getGuestGroups(weddingId)

    override fun getGuestGroupById(weddingId: String, groupId: String): Flow<GuestGroup?> =
        service.getGuestGroupById(weddingId, groupId)

    override suspend fun addGuestGroup(group: GuestGroup): Result<Unit> =
        service.addGuestGroup(group)

    override suspend fun updateGuestGroup(group: GuestGroup): Result<Unit> =
        service.updateGuestGroup(group)

    override suspend fun updateGroupMembers(
        weddingId: String,
        groupId: String,
        members: List<GuestMember>,
    ): Result<Unit> = service.updateGroupMembers(weddingId, groupId, members)

    override suspend fun deleteGuestGroup(weddingId: String, groupId: String): Result<Unit> =
        service.deleteGuestGroup(weddingId, groupId)

    override suspend fun findGroupByInviteToken(token: String): Result<GuestGroup?> =
        service.findByInviteToken(token)

    override suspend fun backfillInviteTokens(groups: List<GuestGroup>) =
        service.backfillInviteTokens(groups)
}
