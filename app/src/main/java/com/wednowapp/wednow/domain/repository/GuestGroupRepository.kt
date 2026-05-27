package com.wednowapp.wednow.domain.repository

import com.wednowapp.wednow.domain.model.GuestGroup
import kotlinx.coroutines.flow.Flow

interface GuestGroupRepository {
    fun getGuestGroups(weddingId: String): Flow<List<GuestGroup>>
    suspend fun addGuestGroup(group: GuestGroup): Result<Unit>
    suspend fun updateGuestGroup(group: GuestGroup): Result<Unit>
    suspend fun deleteGuestGroup(weddingId: String, groupId: String): Result<Unit>
}
