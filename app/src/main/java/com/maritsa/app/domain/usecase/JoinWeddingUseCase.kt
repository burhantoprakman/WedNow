package com.maritsa.app.domain.usecase

import android.content.Context
import com.maritsa.app.core.identity.IdentityManager
import com.maritsa.app.core.session.GuestSessionManager
import com.maritsa.app.core.session.WeddingSessionManager
import com.maritsa.app.domain.model.Guest
import com.maritsa.app.domain.model.GuestRole
import com.maritsa.app.domain.model.WeddingMembership
import com.maritsa.app.domain.repository.GuestGroupRepository
import com.maritsa.app.domain.repository.GuestRepository
import com.maritsa.app.domain.repository.MembershipRepository
import com.maritsa.app.domain.repository.WeddingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

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
        val groupMatch = groupResult.getOrNull()

        if (groupMatch != null) {
            weddingId = groupMatch.weddingId
        } else {
            // 2 — shortCode / weddingId fallback
            val shortCodeResult = weddingRepository.getWeddingByShortCode(code)
            val wedding = shortCodeResult.getOrNull()
                ?: run {
                    // 3 — treat as raw weddingId (QR deep links)
                    weddingRepository.getWeddingById(code).getOrNull()
                }
                ?: return Result.failure(
                    shortCodeResult.exceptionOrNull()
                        ?.let { Exception("Lookup failed: ${it.message}") }
                        ?: Exception("No wedding found for code \"$code\". Check the code and try again.")
                )

            weddingId = wedding.id
        }

        // ── Step 1b: already a member? Just resume the session ────────────────
        // Re-running Step 2 would overwrite the existing Guest doc (wiping RSVP
        // status, since we construct a fresh Guest here) — so if this identity
        // already has a membership for this wedding, skip straight to success.
        val identityId = identityManager.currentIdentityId
        val alreadyMember = membershipRepository.getMemberships(identityId)
            .any { it.weddingId == weddingId }
        if (alreadyMember) {
            WeddingSessionManager.saveWeddingId(context, weddingId)
            saveFcmTokenUseCase(weddingId)
            return Result.success(weddingId)
        }

        // ── Step 2: register the guest in the per-wedding collection ─────────
        val guestId = GuestSessionManager.getGuestId(context)

        val wedding = weddingRepository.getWeddingById(weddingId).getOrNull()
        val role = if (wedding?.adminGuestId == guestId) GuestRole.ADMIN else GuestRole.GUEST

        guestRepository.addGuest(
            weddingId,
            Guest(
                id = guestId,
                name = guestName.orEmpty(),
                role = role,
                groupId = groupMatch?.id,
            )
        ).getOrElse { return Result.failure(it) }

        // ── Step 3: record in the cross-device membership index ───────────────
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
        saveFcmTokenUseCase(weddingId)

        return Result.success(weddingId)
    }
}
