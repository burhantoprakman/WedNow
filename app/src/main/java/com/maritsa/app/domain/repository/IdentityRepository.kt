package com.maritsa.app.domain.repository

import com.maritsa.app.domain.model.Identity

interface IdentityRepository {
    suspend fun saveIdentity(identity: Identity): Result<Unit>
    suspend fun getIdentity(identityId: String): Result<Identity?>

    /** Records that [guestId] was merged into [userIdentityId]. */
    suspend fun markMigrated(guestId: String, userIdentityId: String)
}
