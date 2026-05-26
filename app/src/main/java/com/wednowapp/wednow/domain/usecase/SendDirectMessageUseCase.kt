package com.wednowapp.wednow.domain.usecase

import android.content.Context
import com.wednowapp.wednow.core.session.GuestSessionManager
import com.wednowapp.wednow.domain.model.DirectMessage
import com.wednowapp.wednow.domain.repository.DirectMessageRepository
import com.wednowapp.wednow.domain.repository.GuestRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID
import javax.inject.Inject

class SendDirectMessageUseCase @Inject constructor(
    private val dmRepository: DirectMessageRepository,
    private val guestRepository: GuestRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(
        weddingId: String,
        channelId: String,
        text: String
    ): Result<Unit> {
        val senderId = GuestSessionManager.getGuestId(context)
        val sender = guestRepository.getGuestById(weddingId, senderId).firstOrNull()
        val message = DirectMessage(
            id = UUID.randomUUID().toString(),
            message = text.trim(),
            senderId = senderId,
            senderName = sender?.name.orEmpty(),
            timestamp = System.currentTimeMillis()
        )
        return dmRepository.sendMessage(weddingId, channelId, message)
    }
}
