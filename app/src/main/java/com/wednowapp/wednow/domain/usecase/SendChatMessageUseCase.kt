package com.wednowapp.wednow.domain.usecase

import android.content.Context
import com.wednowapp.wednow.core.session.GuestSessionManager
import com.wednowapp.wednow.domain.model.ChatMessage
import com.wednowapp.wednow.domain.repository.ChatRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject

class SendChatMessageUseCase @Inject constructor(
    private val repository: ChatRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(
        weddingId: String,
        text: String,
        guestName: String
    ): Result<Unit> {
        val guestId = GuestSessionManager.getGuestId(context)
        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            message = text.trim(),
            guestId = guestId,
            guestName = guestName,
            timestamp = System.currentTimeMillis()
        )
        return repository.sendMessage(weddingId, message)
    }
}
