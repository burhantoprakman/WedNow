package com.wednowapp.wednow.domain.usecase

import com.wednowapp.wednow.core.identity.IdentityManager
import com.wednowapp.wednow.domain.model.WeddingMembership
import com.wednowapp.wednow.domain.repository.MembershipRepository
import javax.inject.Inject

/**
 * Returns all [WeddingMembership] records for the current identity.
 *
 * This is used to populate a wedding-switcher UI and to drive cross-device
 * session restore after sign-in.
 *
 * For GUEST identities Firestore may have no entries (the guest joined before
 * the membership index was added, or they just installed the app).  In that case
 * the list will be empty and the guest can still join via short code / QR.
 */
class GetWeddingMembershipsUseCase @Inject constructor(
    private val membershipRepository: MembershipRepository,
    private val identityManager: IdentityManager,
) {
    suspend operator fun invoke(): List<WeddingMembership> =
        membershipRepository.getMemberships(identityManager.currentIdentityId)
}
