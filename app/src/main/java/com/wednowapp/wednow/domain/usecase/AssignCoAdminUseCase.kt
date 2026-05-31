package com.wednowapp.wednow.domain.usecase

import com.wednowapp.wednow.domain.model.GuestRole
import com.wednowapp.wednow.domain.repository.GuestRepository
import com.wednowapp.wednow.domain.repository.MembershipRepository
import javax.inject.Inject

/**
 * Grants the CoAdmin role to a guest already present in the wedding.
 *
 * Updates two records:
 *  1. weddings/{weddingId}/guests/{guestId}  — role field (per-wedding)
 *  2. memberships/{identityId}/weddings/{weddingId}  — role field (cross-device index)
 *
 * The [identityId] parameter is optional; pass it when the guest's identity ID is
 * known (i.e. when they have already been linked to a Firebase account).  If blank
 * only the per-wedding guest document is updated.
 *
 * Revocation: pass [revoke] = true to downgrade the role back to GUEST.
 */
class AssignCoAdminUseCase @Inject constructor(
    private val guestRepository: GuestRepository,
    private val membershipRepository: MembershipRepository,
) {
    suspend operator fun invoke(
        weddingId: String,
        guestId: String,
        identityId: String = "",
        revoke: Boolean = false,
    ): Result<Unit> {
        val newRole = if (revoke) GuestRole.GUEST else GuestRole.COADMIN

        // 1. Update the per-wedding guest document
        val guestResult = guestRepository.updateGuestRole(weddingId, guestId, newRole)
        if (guestResult.isFailure) return guestResult

        // 2. Update the cross-device membership index (best-effort)
        if (identityId.isNotBlank()) {
            membershipRepository.updateRole(identityId, weddingId, newRole)
        }

        return Result.success(Unit)
    }
}
