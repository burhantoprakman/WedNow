package com.wednowapp.wednow.data.repository

import com.wednowapp.wednow.data.remote.IdentityFirestoreService
import com.wednowapp.wednow.domain.model.Identity
import com.wednowapp.wednow.domain.repository.IdentityRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdentityRepositoryImpl @Inject constructor(
    private val service: IdentityFirestoreService,
) : IdentityRepository {

    override suspend fun saveIdentity(identity: Identity) = service.saveIdentity(identity)

    override suspend fun getIdentity(identityId: String) = service.getIdentity(identityId)

    override suspend fun markMigrated(guestId: String, userIdentityId: String) =
        service.markMigrated(guestId, userIdentityId)
}
