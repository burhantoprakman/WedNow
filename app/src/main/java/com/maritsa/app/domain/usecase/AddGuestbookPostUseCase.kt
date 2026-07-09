package com.maritsa.app.domain.usecase

import android.content.Context
import android.net.Uri
import com.maritsa.app.core.identity.IdentityManager
import com.maritsa.app.core.session.GuestSessionManager
import com.maritsa.app.domain.model.GuestbookPost
import com.maritsa.app.domain.repository.GuestbookRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject

class AddGuestbookPostUseCase @Inject constructor(
    private val repository: GuestbookRepository,
    private val identityManager: IdentityManager,
    @ApplicationContext private val context: Context,
) {
    suspend operator fun invoke(
        weddingId: String,
        message: String,
        photoUris: List<Uri> = emptyList(),
    ): Result<Unit> {
        val guestId = GuestSessionManager.getGuestId(context)
        val senderName = GuestSessionManager.getGuestName(context).ifBlank { "A Guest" }
        val identityId = identityManager.currentIdentityId   // UUID (guest) or Firebase UID (user)
        val postId = UUID.randomUUID().toString()

        // Upload photos first — fail fast if any upload fails
        val photoUrls = if (photoUris.isEmpty()) {
            emptyList()
        } else {
            repository.uploadPhotos(weddingId, postId, photoUris)
                .getOrElse { return Result.failure(it) }
        }

        val post = GuestbookPost(
            id = postId,
            guestId = guestId,
            senderName = senderName,
            message = message.trim(),
            photoUrls = photoUrls,
            timestamp = System.currentTimeMillis(),
            ownerUserId = identityId,   // kept for legacy compat
            ownerIdentityId = identityId,   // unified ownership field
        )
        return repository.addPost(weddingId, post)
    }
}
