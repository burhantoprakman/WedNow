package com.maritsa.app.fake

import com.maritsa.app.domain.model.WeddingMembership
import com.maritsa.app.domain.repository.MembershipRepository

class FakeMembershipRepository : MembershipRepository {

    /** identityId → (weddingId → WeddingMembership) */
    private val store = mutableMapOf<String, MutableMap<String, WeddingMembership>>()

    var addShouldFail = false
    var addCallCount = 0

    override suspend fun addMembership(membership: WeddingMembership): Result<Unit> {
        addCallCount++
        if (addShouldFail) return Result.failure(RuntimeException("Fake add failure"))
        store.getOrPut(membership.identityId) { mutableMapOf() }[membership.weddingId] = membership
        return Result.success(Unit)
    }

    override suspend fun getMemberships(identityId: String): List<WeddingMembership> =
        store[identityId]?.values?.toList() ?: emptyList()

    override suspend fun removeMembership(identityId: String, weddingId: String): Result<Unit> {
        store[identityId]?.remove(weddingId)
        return Result.success(Unit)
    }

    override suspend fun updateRole(
        identityId: String,
        weddingId: String,
        role: String
    ): Result<Unit> {
        val membership = store[identityId]?.get(weddingId)
            ?: return Result.failure(NoSuchElementException("Membership not found"))
        store[identityId]!![weddingId] = membership.copy(role = role)
        return Result.success(Unit)
    }

    fun getMembership(identityId: String, weddingId: String): WeddingMembership? =
        store[identityId]?.get(weddingId)

    fun totalMemberships(): Int = store.values.sumOf { it.size }

    fun reset() {
        store.clear()
        addShouldFail = false
        addCallCount = 0
    }
}
