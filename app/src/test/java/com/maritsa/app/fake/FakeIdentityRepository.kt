package com.maritsa.app.fake

import com.maritsa.app.domain.model.Identity
import com.maritsa.app.domain.repository.IdentityRepository

class FakeIdentityRepository : IdentityRepository {

    val identities = mutableMapOf<String, Identity>()

    /** guestId → userIdentityId migration records */
    val migrations = mutableMapOf<String, String>()

    var saveShouldFail = false
    var saveCallCount = 0

    override suspend fun saveIdentity(identity: Identity): Result<Unit> {
        saveCallCount++
        if (saveShouldFail) return Result.failure(RuntimeException("Fake save failure"))
        identities[identity.identityId] = identity
        return Result.success(Unit)
    }

    override suspend fun getIdentity(identityId: String): Result<Identity?> =
        Result.success(identities[identityId])

    override suspend fun markMigrated(guestId: String, userIdentityId: String) {
        migrations[guestId] = userIdentityId
    }

    fun reset() {
        identities.clear()
        migrations.clear()
        saveShouldFail = false
        saveCallCount = 0
    }
}
