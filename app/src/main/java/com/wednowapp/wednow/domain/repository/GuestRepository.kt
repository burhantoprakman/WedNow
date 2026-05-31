package com.wednowapp.wednow.domain.repository

import com.wednowapp.wednow.domain.model.Guest
import kotlinx.coroutines.flow.Flow

interface GuestRepository {
    suspend fun addGuest(weddingId: String, guest: Guest): Result<Unit>
    fun getGuests(weddingId: String): Flow<List<Guest>>
    fun getGuestById(weddingId: String, guestId: String): Flow<Guest?>
    suspend fun updateGuest(weddingId: String, guest: Guest): Result<Unit>

    /** Partially updates only the role field of a guest document. */
    suspend fun updateGuestRole(weddingId: String, guestId: String, role: String): Result<Unit>
    suspend fun updateRsvp(weddingId: String, guestId: String, status: String): Result<Unit>
    suspend fun deleteGuest(weddingId: String, guestId: String): Result<Unit>
}
