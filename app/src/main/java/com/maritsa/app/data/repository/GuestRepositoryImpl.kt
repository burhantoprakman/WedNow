package com.maritsa.app.data.repository

import com.maritsa.app.data.remote.GuestFirestoreService
import com.maritsa.app.domain.model.Guest
import com.maritsa.app.domain.repository.GuestRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuestRepositoryImpl @Inject constructor(
    private val service: GuestFirestoreService
) : GuestRepository {

    override suspend fun addGuest(weddingId: String, guest: Guest): Result<Unit> =
        service.addGuest(weddingId, guest)

    override fun getGuests(weddingId: String): Flow<List<Guest>> =
        service.getGuests(weddingId)

    override fun getGuestById(weddingId: String, guestId: String): Flow<Guest?> =
        service.getGuestById(weddingId, guestId)

    override suspend fun updateGuest(weddingId: String, guest: Guest): Result<Unit> =
        service.updateGuest(weddingId, guest)

    override suspend fun updateGuestRole(weddingId: String, guestId: String, role: String) =
        service.updateGuestRole(weddingId, guestId, role)

    override suspend fun updateRsvp(
        weddingId: String,
        guestId: String,
        status: String
    ): Result<Unit> =
        service.updateRsvp(weddingId, guestId, status)

    override suspend fun deleteGuest(weddingId: String, guestId: String): Result<Unit> =
        service.deleteGuest(weddingId, guestId)
}
