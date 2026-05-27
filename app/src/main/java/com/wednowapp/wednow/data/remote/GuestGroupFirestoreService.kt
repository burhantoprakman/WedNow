package com.wednowapp.wednow.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.wednowapp.wednow.domain.model.GuestGroup
import com.wednowapp.wednow.domain.model.GuestMember
import com.wednowapp.wednow.domain.model.MemberRole
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuestGroupFirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private fun groupsRef(weddingId: String) =
        firestore.collection("weddings").document(weddingId).collection("guestGroups")

    fun getGuestGroups(weddingId: String): Flow<List<GuestGroup>> = callbackFlow {
        val listener = groupsRef(weddingId)
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                val groups =
                    snapshot?.documents?.mapNotNull { it.toGuestGroup(weddingId) } ?: emptyList()
                trySend(groups)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addGuestGroup(group: GuestGroup): Result<Unit> = runCatching {
        groupsRef(group.weddingId).document(group.id).set(group.toMap()).await()
    }

    suspend fun updateGuestGroup(group: GuestGroup): Result<Unit> = runCatching {
        groupsRef(group.weddingId).document(group.id).set(group.toMap()).await()
    }

    suspend fun deleteGuestGroup(weddingId: String, groupId: String): Result<Unit> = runCatching {
        groupsRef(weddingId).document(groupId).delete().await()
    }

    private fun GuestGroup.toMap(): Map<String, Any?> = mapOf(
        "familyName" to familyName,
        "inviteToken" to inviteToken,
        "invitationLink" to invitationLink,
        "members" to members.map { m ->
            mapOf(
                "name" to m.name,
                "role" to m.role,
                "plusOneAllowed" to m.plusOneAllowed,
            )
        },
        "rsvpStatus" to rsvpStatus,
        "invitationOpened" to invitationOpened,
        "createdAt" to createdAt,
    )

    private fun DocumentSnapshot.toGuestGroup(weddingId: String): GuestGroup? {
        if (!exists()) return null
        return runCatching {
            val rawMembers = (get("members") as? List<*>) ?: emptyList<Any>()
            val members = rawMembers.filterIsInstance<Map<*, *>>().map { m ->
                GuestMember(
                    name = m["name"] as? String ?: "",
                    role = m["role"] as? String ?: MemberRole.ADULT,
                    plusOneAllowed = m["plusOneAllowed"] as? Boolean ?: false,
                )
            }
            GuestGroup(
                id = id,
                weddingId = weddingId,
                familyName = getString("familyName") ?: "",
                inviteToken = getString("inviteToken") ?: "",
                invitationLink = getString("invitationLink") ?: "",
                members = members,
                rsvpStatus = getString("rsvpStatus"),
                invitationOpened = getBoolean("invitationOpened") ?: false,
                createdAt = getLong("createdAt") ?: 0L,
            )
        }.getOrNull()
    }
}
