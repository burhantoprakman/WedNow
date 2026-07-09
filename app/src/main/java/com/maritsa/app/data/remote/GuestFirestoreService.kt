package com.maritsa.app.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.maritsa.app.domain.model.Guest
import com.maritsa.app.domain.model.GuestRole
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuestFirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun guestsRef(weddingId: String) =
        firestore.collection("weddings").document(weddingId).collection("guests")

    suspend fun addGuest(weddingId: String, guest: Guest): Result<Unit> = runCatching {
        guestsRef(weddingId).document(guest.id).set(guest.toMap()).await()
    }

    fun getGuests(weddingId: String): Flow<List<Guest>> = callbackFlow {
        val listener = guestsRef(weddingId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error); return@addSnapshotListener
            }
            val guests = snapshot?.documents?.mapNotNull { it.toGuest() } ?: emptyList()
            trySend(guests)
        }
        awaitClose { listener.remove() }
    }

    fun getGuestById(weddingId: String, guestId: String): Flow<Guest?> = callbackFlow {
        val listener = guestsRef(weddingId).document(guestId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                trySend(snapshot?.toGuest())
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateGuest(weddingId: String, guest: Guest): Result<Unit> = runCatching {
        guestsRef(weddingId).document(guest.id).set(guest.toMap()).await()
    }

    /** Partial update — only touches the role field, leaving all other fields intact. */
    suspend fun updateGuestRole(weddingId: String, guestId: String, role: String): Result<Unit> =
        runCatching {
            guestsRef(weddingId).document(guestId).update("role", role).await()
        }

    // Partial update — only touches rsvpStatus and rsvpUpdatedAt, leaving all other fields intact.
    suspend fun updateRsvp(weddingId: String, guestId: String, status: String): Result<Unit> =
        runCatching {
            guestsRef(weddingId).document(guestId).update(
                mapOf(
                    "rsvpStatus" to status,
                    "rsvpUpdatedAt" to System.currentTimeMillis()
                )
            ).await()
        }

    suspend fun deleteGuest(weddingId: String, guestId: String): Result<Unit> = runCatching {
        guestsRef(weddingId).document(guestId).delete().await()
    }

    private fun Guest.toMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "role" to role,
        "rsvpStatus" to rsvpStatus,
        "rsvpUpdatedAt" to rsvpUpdatedAt,
        "groupId" to groupId,
    )

    private fun DocumentSnapshot.toGuest(): Guest? {
        if (!exists()) return null
        return runCatching {
            Guest(
                id = id,
                name = getString("name") ?: "",
                role = getString("role") ?: GuestRole.GUEST,
                rsvpStatus = getString("rsvpStatus"),
                rsvpUpdatedAt = getLong("rsvpUpdatedAt"),
                groupId = getString("groupId"),
            )
        }.getOrNull()
    }
}
