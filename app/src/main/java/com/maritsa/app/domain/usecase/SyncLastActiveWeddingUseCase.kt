package com.maritsa.app.domain.usecase

import android.content.Context
import com.maritsa.app.core.identity.IdentityManager
import com.maritsa.app.core.session.WeddingSessionManager
import com.maritsa.app.domain.model.IdentityType
import com.maritsa.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Resolves the best wedding ID to open after a sign-in.
 *
 * Strategy (local-first to minimise latency):
 *
 *  1. Check [WeddingSessionManager] (local SharedPreferences).
 *  2. If nothing local AND the identity is USER, query Firestore
 *     [UserPreferences.lastActiveWeddingId] for cross-device restore.
 *  3. Return the resolved ID, or null if no wedding is known.
 *
 * The local value is always saved when a user enters a wedding, so this
 * Firestore call only matters on a brand-new device after first sign-in.
 */
class SyncLastActiveWeddingUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val identityManager: IdentityManager,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke(): String? {
        // 1. Local check — always fast
        val local = WeddingSessionManager.getWeddingId(context)
        if (local != null) return local

        // 2. Firestore restore — only for authenticated users on a fresh install
        if (identityManager.currentIdentity.type == IdentityType.USER) {
            val prefs = userPreferencesRepository
                .getPreferences(identityManager.currentIdentityId)
                .getOrNull()
            val remote = prefs?.lastActiveWeddingId
            if (remote != null) {
                // Persist locally so subsequent launches skip this Firestore call
                WeddingSessionManager.saveWeddingId(context, remote)
            }
            return remote
        }

        return null
    }
}
