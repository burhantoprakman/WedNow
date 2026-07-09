package com.maritsa.app.domain.usecase

import android.content.Context
import com.maritsa.app.core.identity.IdentityManager
import com.maritsa.app.core.session.WeddingSessionManager
import com.maritsa.app.domain.model.IdentityType
import com.maritsa.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Records [weddingId] as the last-active wedding for the current identity.
 *
 * Always writes to local [WeddingSessionManager] for fast access on next launch.
 *
 * For USER identities also writes to Firestore so the preference survives a
 * fresh install and can be restored on any device after sign-in.
 *
 * Call this whenever the user opens (or creates) a wedding.
 */
class UpdateLastActiveWeddingUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val identityManager: IdentityManager,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke(weddingId: String) {
        // Always persist locally
        WeddingSessionManager.saveWeddingId(context, weddingId)

        // Persist to Firestore only for authenticated users
        if (identityManager.currentIdentity.type == IdentityType.USER) {
            userPreferencesRepository.updateLastActiveWedding(
                identityId = identityManager.currentIdentityId,
                weddingId = weddingId,
            )
        }
    }
}
