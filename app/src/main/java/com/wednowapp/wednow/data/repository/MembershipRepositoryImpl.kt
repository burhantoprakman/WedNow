package com.wednowapp.wednow.data.repository

import com.wednowapp.wednow.data.remote.MembershipFirestoreService
import com.wednowapp.wednow.domain.model.WeddingMembership
import com.wednowapp.wednow.domain.repository.MembershipRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MembershipRepositoryImpl @Inject constructor(
    private val service: MembershipFirestoreService,
) : MembershipRepository {

    override suspend fun addMembership(membership: WeddingMembership) =
        service.addMembership(membership)

    override suspend fun getMemberships(identityId: String): List<WeddingMembership> =
        service.getMemberships(identityId).getOrDefault(emptyList())

    override suspend fun removeMembership(identityId: String, weddingId: String) =
        service.removeMembership(identityId, weddingId)

    override suspend fun updateRole(identityId: String, weddingId: String, role: String) =
        service.updateRole(identityId, weddingId, role)
}
