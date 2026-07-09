package com.maritsa.app.fake

import com.maritsa.app.domain.model.Guest
import com.maritsa.app.domain.repository.GuestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeGuestRepository : GuestRepository {

    /** weddingId → (guestId → Guest) */
    private val store = mutableMapOf<String, MutableMap<String, Guest>>()
    private val flows = mutableMapOf<String, MutableStateFlow<List<Guest>>>()

    var addShouldFail = false

    override suspend fun addGuest(weddingId: String, guest: Guest): Result<Unit> {
        if (addShouldFail) return Result.failure(RuntimeException("Fake add failure"))
        store.getOrPut(weddingId) { mutableMapOf() }[guest.id] = guest
        flows[weddingId]?.value = store[weddingId]?.values?.toList() ?: emptyList()
        return Result.success(Unit)
    }

    override fun getGuests(weddingId: String): Flow<List<Guest>> {
        val flow = flows.getOrPut(weddingId) {
            MutableStateFlow(store[weddingId]?.values?.toList() ?: emptyList())
        }
        return flow
    }

    override fun getGuestById(weddingId: String, guestId: String): Flow<Guest?> =
        getGuests(weddingId).map { it.find { g -> g.id == guestId } }

    override suspend fun updateGuest(weddingId: String, guest: Guest): Result<Unit> {
        store[weddingId]?.set(guest.id, guest)
        flows[weddingId]?.value = store[weddingId]?.values?.toList() ?: emptyList()
        return Result.success(Unit)
    }

    override suspend fun updateGuestRole(
        weddingId: String,
        guestId: String,
        role: String,
    ): Result<Unit> {
        val guest = store[weddingId]?.get(guestId)
            ?: return Result.failure(NoSuchElementException("Guest $guestId not found in $weddingId"))
        store[weddingId]!![guestId] = guest.copy(role = role)
        flows[weddingId]?.value = store[weddingId]?.values?.toList() ?: emptyList()
        return Result.success(Unit)
    }

    override suspend fun updateRsvp(
        weddingId: String,
        guestId: String,
        status: String,
    ): Result<Unit> {
        val guest = store[weddingId]?.get(guestId) ?: return Result.success(Unit)
        store[weddingId]!![guestId] = guest.copy(rsvpStatus = status)
        return Result.success(Unit)
    }

    override suspend fun deleteGuest(weddingId: String, guestId: String): Result<Unit> {
        store[weddingId]?.remove(guestId)
        flows[weddingId]?.value = store[weddingId]?.values?.toList() ?: emptyList()
        return Result.success(Unit)
    }

    fun getGuest(weddingId: String, guestId: String): Guest? = store[weddingId]?.get(guestId)

    fun reset() {
        store.clear()
        flows.clear()
        addShouldFail = false
    }
}
