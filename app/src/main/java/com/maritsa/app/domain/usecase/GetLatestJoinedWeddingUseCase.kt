package com.maritsa.app.domain.usecase

import android.content.Context
import com.maritsa.app.core.session.WeddingSessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Resolves the signed-in identity's most recently joined wedding (by
 * [com.maritsa.app.domain.model.WeddingMembership.joinedAt]) and, if one exists,
 * resumes the session for it exactly as [JoinWeddingUseCase] does when the
 * identity is already a member — so the app opens straight to WeddingHome next
 * launch and push notifications keep working, without requiring a join code.
 */
class GetLatestJoinedWeddingUseCase @Inject constructor(
    private val getWeddingMembershipsUseCase: GetWeddingMembershipsUseCase,
    private val saveFcmTokenUseCase: SaveFcmTokenUseCase,
    @ApplicationContext private val context: Context,
) {
    suspend operator fun invoke(): String? {
        val weddingId = getWeddingMembershipsUseCase()
            .maxByOrNull { it.joinedAt }
            ?.weddingId
            ?: return null

        WeddingSessionManager.saveWeddingId(context, weddingId)
        saveFcmTokenUseCase(weddingId)
        return weddingId
    }
}
