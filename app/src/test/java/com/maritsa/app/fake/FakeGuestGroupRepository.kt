package com.maritsa.app.fake

import com.maritsa.app.domain.model.GuestGroup
import com.maritsa.app.domain.model.GuestMember
import com.maritsa.app.domain.repository.GuestGroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeGuestGroupRepository : GuestGroupRepository {

    private val groups = mutableMapOf<String, GuestGroup>()

    /** inviteToken → GuestGroup */
    private val tokenIndex = mutableMapOf<String, GuestGroup>()

    override fun getGuestGroups(weddingId: String): Flow<List<GuestGroup>> =
        flowOf(groups.values.filter { it.weddingId == weddingId })

    override fun getGuestGroupById(weddingId: String, groupId: String): Flow<GuestGroup?> =
        flowOf(groups[groupId]?.takeIf { it.weddingId == weddingId })

    override suspend fun updateGroupMembers(
        weddingId: String,
        groupId: String,
        members: List<GuestMember>,
    ): Result<Unit> {
        val existing = groups[groupId] ?: return Result.failure(NoSuchElementException(groupId))
        groups[groupId] = existing.copy(members = members)
        return Result.success(Unit)
    }

    /** Test helper — adds a group under a specific weddingId for readability in test set-up. */
    suspend fun addGuestGroup(weddingId: String, group: GuestGroup): Result<Unit> =
        addGuestGroup(group.copy(weddingId = weddingId))

    override suspend fun addGuestGroup(group: GuestGroup): Result<Unit> {
        val stored = group
        groups[stored.id] = stored
        tokenIndex[stored.inviteToken] = stored
        return Result.success(Unit)
    }

    override suspend fun updateGuestGroup(group: GuestGroup): Result<Unit> {
        groups[group.id] = group
        tokenIndex[group.inviteToken] = group
        return Result.success(Unit)
    }

    override suspend fun deleteGuestGroup(weddingId: String, groupId: String): Result<Unit> {
        groups.remove(groupId)
        tokenIndex.entries.removeIf { it.value.id == groupId }
        return Result.success(Unit)
    }

    override suspend fun findGroupByInviteToken(token: String): Result<GuestGroup?> =
        Result.success(tokenIndex[token])

    override suspend fun backfillInviteTokens(groups: List<GuestGroup>) {
        groups.forEach { group -> tokenIndex[group.inviteToken] = group }
    }

    fun reset() {
        groups.clear(); tokenIndex.clear()
    }
}
