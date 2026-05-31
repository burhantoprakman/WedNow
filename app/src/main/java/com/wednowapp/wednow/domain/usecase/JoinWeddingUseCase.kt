package com.wednowapp.wednow.domain.usecase

import android.content.Context
import android.util.Log
import com.wednowapp.wednow.core.identity.IdentityManager
import com.wednowapp.wednow.core.session.GuestSessionManager
import com.wednowapp.wednow.core.session.WeddingSessionManager
import com.wednowapp.wednow.domain.model.Guest
import com.wednowapp.wednow.domain.model.GuestRole
import com.wednowapp.wednow.domain.model.WeddingMembership
import com.wednowapp.wednow.domain.repository.GuestGroupRepository
import com.wednowapp.wednow.domain.repository.GuestRepository
import com.wednowapp.wednow.domain.repository.MembershipRepository
import com.wednowapp.wednow.domain.repository.WeddingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val TAG = "JoinWedding"

class JoinWeddingUseCase @Inject constructor(
    private val weddingRepository: WeddingRepository,
    private val guestRepository: GuestRepository,
    private val guestGroupRepository: GuestGroupRepository,
    private val membershipRepository: MembershipRepository,
    private val identityManager: IdentityManager,
    private val saveFcmTokenUseCase: SaveFcmTokenUseCase,
    @ApplicationContext private val context: Context,
) {
    suspend operator fun invoke(weddingCode: String, guestName: String?): Result<String> {
        val code = weddingCode.uppercase().trim()

        // ── Step 1: resolve the weddingId from whichever code type was entered ──
        //
        // Priority:
        //  1. inviteToken  — per-family personalised invitation code
        //  2. shortCode    — wedding-level event code
        //  3. weddingId    — raw Firestore document ID (QR deep links)

        val weddingId: String

        // 1 — inviteToken lookup
        val groupResult = guestGroupRepository.findGroupByInviteToken(code)
        Log.d(
            TAG, "inviteToken lookup '$code': success=${groupResult.isSuccess}, " +
                    "group=${groupResult.getOrNull()?.id}, error=${groupResult.exceptionOrNull()?.message}"
        )

        val groupMatch = groupResult.getOrNull()
        if (groupMatch != null) {
            weddingId = groupMatch.weddingId
            Log.d(TAG, "Joined via inviteToken → weddingId=$weddingId")
        } else {
            // 2 — shortCode / weddingId fallback
            val shortCodeResult = weddingRepository.getWeddingByShortCode(code)
            Log.d(
                TAG, "shortCode lookup '$code': success=${shortCodeResult.isSuccess}, " +
                        "wedding=${shortCodeResult.getOrNull()?.id}, " +
                        "error=${shortCodeResult.exceptionOrNull()?.message}"
            )

            val wedding = shortCodeResult.getOrNull()
                ?: run {
                    // 3 — treat as raw weddingId (QR deep links)
                    val byIdResult = weddingRepository.getWeddingById(code)
                    Log.d(
                        TAG, "weddingId fallback '$code': success=${byIdResult.isSuccess}, " +
                                "error=${byIdResult.exceptionOrNull()?.message}"
                    )
                    byIdResult.getOrNull()
                }
                ?: return Result.failure(
                    shortCodeResult.exceptionOrNull()
                        ?.let { Exception("Lookup failed: ${it.message}") }
                        ?: Exception("No wedding found for code \"$code\". Check the code and try again.")
                )

            weddingId = wedding.id
        }

        // ── Step 2: register the guest in the per-wedding collection ─────────
        val guestId = GuestSessionManager.getGuestId(context)

        // Determine role (only the original creator gets ADMIN through this flow)
        val wedding = weddingRepository.getWeddingById(weddingId).getOrNull()
        val role = if (wedding?.adminGuestId == guestId) GuestRole.ADMIN else GuestRole.GUEST

        guestRepository.addGuest(
            weddingId,
            Guest(id = guestId, name = guestName.orEmpty(), role = role)
        ).getOrElse { return Result.failure(it) }

        // ── Step 3: record in the cross-device membership index ───────────────
        // This is best-effort — failure here does not abort the join
        val identityId = identityManager.currentIdentityId
        runCatching {
            membershipRepository.addMembership(
                WeddingMembership(
                    weddingId = weddingId,
                    identityId = identityId,
                    role = role,
                    joinedAt = System.currentTimeMillis(),
                )
            )
        }

        WeddingSessionManager.saveWeddingId(context, weddingId)
        GuestSessionManager.saveGuestName(context, guestName.orEmpty())
        saveFcmTokenUseCase(weddingId) // best-effort; ignore failure

        return Result.success(weddingId)
    }
}
