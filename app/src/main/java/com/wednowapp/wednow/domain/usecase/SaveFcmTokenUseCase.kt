package com.wednowapp.wednow.domain.usecase

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.wednowapp.wednow.core.session.GuestSessionManager
import com.wednowapp.wednow.data.remote.FcmTokenService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SaveFcmTokenUseCase @Inject constructor(
    private val fcmTokenService: FcmTokenService,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(weddingId: String): Result<Unit> {
        val guestId = GuestSessionManager.getGuestId(context)
        val token = runCatching {
            FirebaseMessaging.getInstance().token.await()
        }.getOrElse { return Result.failure(it) }
        return fcmTokenService.saveToken(weddingId, guestId, token)
    }
}
