package com.wednowapp.wednow.domain.usecase

import android.content.Context
import com.wednowapp.wednow.core.session.GuestSessionManager
import com.wednowapp.wednow.domain.model.Guest
import com.wednowapp.wednow.domain.repository.GuestRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentGuestUseCase @Inject constructor(
    private val repository: GuestRepository,
    @ApplicationContext private val context: Context
) {
    operator fun invoke(weddingId: String): Flow<Guest?> {
        val guestId = GuestSessionManager.getGuestId(context)
        return repository.getGuestById(weddingId, guestId)
    }
}
