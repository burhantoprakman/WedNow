package com.maritsa.app.domain.repository

import com.maritsa.app.domain.model.WeddingMembership

interface MembershipRepository {
    suspend fun addMembership(membership: WeddingMembership): Result<Unit>

    /** Returns all memberships for [identityId]; empty list on failure or no memberships. */
    suspend fun getMemberships(identityId: String): List<WeddingMembership>
    suspend fun removeMembership(identityId: String, weddingId: String): Result<Unit>
    suspend fun updateRole(identityId: String, weddingId: String, role: String): Result<Unit>
}
