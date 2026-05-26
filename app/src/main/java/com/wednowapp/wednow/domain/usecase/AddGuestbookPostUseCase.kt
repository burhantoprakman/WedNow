package com.wednowapp.wednow.domain.usecase

import android.content.Context
import com.wednowapp.wednow.core.session.GuestSessionManager
import com.wednowapp.wednow.domain.model.GuestbookPost
import com.wednowapp.wednow.domain.repository.GuestbookRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject

class AddGuestbookPostUseCase @Inject constructor(
    private val repository: GuestbookRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(weddingId: String, message: String): Result<Unit> {
        val guestId = GuestSessionManager.getGuestId(context)
        val post = GuestbookPost(
            id = UUID.randomUUID().toString(),
            guestId = guestId,
            message = message.trim(),
            timestamp = System.currentTimeMillis()
        )
        return repository.addPost(weddingId, post)
    }
}
