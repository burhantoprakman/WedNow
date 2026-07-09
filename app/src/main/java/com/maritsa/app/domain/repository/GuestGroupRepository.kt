package com.maritsa.app.domain.repository

import com.maritsa.app.domain.model.GuestGroup
import com.maritsa.app.domain.model.GuestMember
import kotlinx.coroutines.flow.Flow

interface GuestGroupRepository {
    fun getGuestGroups(weddingId: String): Flow<List<GuestGroup>>
    fun getGuestGroupById(weddingId: String, groupId: String): Flow<GuestGroup?>
    suspend fun addGuestGroup(group: GuestGroup): Result<Unit>
    suspend fun updateGuestGroup(group: GuestGroup): Result<Unit>
    suspend fun updateGroupMembers(
        weddingId: String,
        groupId: String,
        members: List<GuestMember>
    ): Result<Unit>

    suspend fun deleteGuestGroup(weddingId: String, groupId: String): Result<Unit>

    /** Resolve a per-group inviteToken to the [GuestGroup] that owns it. */
    suspend fun findGroupByInviteToken(token: String): Result<GuestGroup?>

    /**
     * Write missing inviteTokens reverse-lookup entries for all [groups].
     * Call once when the admin opens Guest Management so old groups become joinable.
     */
    suspend fun backfillInviteTokens(groups: List<GuestGroup>)
}
