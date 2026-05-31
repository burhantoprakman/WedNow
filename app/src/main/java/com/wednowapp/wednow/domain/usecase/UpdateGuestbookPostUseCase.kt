package com.wednowapp.wednow.domain.usecase

import android.net.Uri
import com.wednowapp.wednow.domain.model.GuestbookPost
import com.wednowapp.wednow.domain.repository.GuestbookRepository
import javax.inject.Inject

class UpdateGuestbookPostUseCase @Inject constructor(
    private val repository: GuestbookRepository,
) {
    /**
     * Saves edits to an existing guestbook post, handling all photo mutations:
     *
     * - [post] must already have [GuestbookPost.photoUrls] set to the URLs the user
     *   chose to **keep** (i.e. original list minus any removed ones).
     * - [newPhotoUris] are local [Uri]s the user added during editing; they will
     *   be uploaded and appended to `post.photoUrls`.
     * - [removedPhotoUrls] are URLs that were in the original post but were removed;
     *   their Storage files are deleted best-effort (failure does not abort the save).
     */
    suspend operator fun invoke(
        weddingId: String,
        post: GuestbookPost,
        newPhotoUris: List<Uri> = emptyList(),
        removedPhotoUrls: List<String> = emptyList(),
    ): Result<Unit> {
        // 1. Upload newly added photos (timestamp-safe names, won't overwrite originals)
        val newUrls = if (newPhotoUris.isEmpty()) {
            emptyList()
        } else {
            repository.uploadEditPhotos(weddingId, post.id, newPhotoUris)
                .getOrElse { return Result.failure(it) }
        }

        // 2. Delete removed photos from Storage (best-effort — never fail the save)
        removedPhotoUrls.forEach { url ->
            runCatching { repository.deletePhoto(url) }
        }

        // 3. Write final post to Firestore (kept URLs + newly uploaded URLs)
        val finalPost = post.copy(photoUrls = post.photoUrls + newUrls)
        return repository.updatePost(weddingId, finalPost)
    }
}
