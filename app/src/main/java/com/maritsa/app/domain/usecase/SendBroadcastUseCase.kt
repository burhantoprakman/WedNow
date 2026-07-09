package com.maritsa.app.domain.usecase

import android.content.Context
import com.maritsa.app.core.session.GuestSessionManager
import com.maritsa.app.domain.model.Broadcast
import com.maritsa.app.domain.repository.BroadcastRepository
import com.maritsa.app.domain.repository.GuestRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID
import javax.inject.Inject

class SendBroadcastUseCase @Inject constructor(
    private val broadcastRepository: BroadcastRepository,
    private val guestRepository: GuestRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(weddingId: String, message: String): Result<Unit> {
        val guestId = GuestSessionManager.getGuestId(context)
        val guest = guestRepository.getGuestById(weddingId, guestId).firstOrNull()
        val broadcast = Broadcast(
            id = UUID.randomUUID().toString(),
            message = message.trim(),
            sentBy = guestId,
            sentByName = guest?.name.orEmpty(),
            timestamp = System.currentTimeMillis()
        )
        return broadcastRepository.sendBroadcast(weddingId, broadcast)
    }
}
