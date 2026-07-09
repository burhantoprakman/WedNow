package com.maritsa.app.permissions

import com.maritsa.app.domain.model.ContentPermissions
import com.maritsa.app.domain.model.GuestRole
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ContentPermissionsTest {

    // ── canEdit ───────────────────────────────────────────────────────────────

    @Nested
    inner class CanEdit {

        @Test
        fun `given matching identity IDs, canEdit returns true`() {
            val id = "user-abc-123"
            assertTrue(ContentPermissions.canEdit(id, id))
        }

        @Test
        fun `given different identity IDs, canEdit returns false`() {
            assertFalse(ContentPermissions.canEdit("owner-id", "requester-id"))
        }

        @Test
        fun `given blank ownerIdentityId, canEdit returns false`() {
            assertFalse(ContentPermissions.canEdit("", "requester-id"))
        }

        @Test
        fun `given blank currentIdentityId, canEdit returns false`() {
            assertFalse(ContentPermissions.canEdit("owner-id", ""))
        }

        @Test
        fun `given both IDs blank, canEdit returns false`() {
            assertFalse(ContentPermissions.canEdit("", ""))
        }
    }

    // ── canDelete ─────────────────────────────────────────────────────────────

    @Nested
    inner class CanDelete {

        @Test
        fun `given ADMIN role, canDelete returns true regardless of ownership`() {
            assertTrue(
                ContentPermissions.canDelete(
                    ownerIdentityId = "someone-else",
                    currentIdentityId = "admin-user",
                    role = GuestRole.ADMIN,
                )
            )
        }

        @Test
        fun `given COADMIN role, canDelete returns true regardless of ownership`() {
            assertTrue(
                ContentPermissions.canDelete(
                    ownerIdentityId = "someone-else",
                    currentIdentityId = "coadmin-user",
                    role = GuestRole.COADMIN,
                )
            )
        }

        @Test
        fun `given owner with GUEST role, canDelete returns true`() {
            val id = "owner-id"
            assertTrue(
                ContentPermissions.canDelete(
                    ownerIdentityId = id,
                    currentIdentityId = id,
                    role = GuestRole.GUEST,
                )
            )
        }

        @Test
        fun `given non-owner with GUEST role, canDelete returns false`() {
            assertFalse(
                ContentPermissions.canDelete(
                    ownerIdentityId = "owner-id",
                    currentIdentityId = "other-user",
                    role = GuestRole.GUEST,
                )
            )
        }

        @Test
        fun `given non-owner with null role, canDelete returns false`() {
            assertFalse(
                ContentPermissions.canDelete(
                    ownerIdentityId = "owner-id",
                    currentIdentityId = "other-user",
                    role = null,
                )
            )
        }

        @Test
        fun `given blank ownerIdentityId with matching legacyOwnerId, canDelete returns true`() {
            val legacyId = "legacy-guest-uuid"
            assertTrue(
                ContentPermissions.canDelete(
                    ownerIdentityId = "",
                    currentIdentityId = legacyId,
                    role = GuestRole.GUEST,
                    legacyOwnerId = legacyId,
                )
            )
        }

        @Test
        fun `given blank ownerIdentityId with non-matching legacyOwnerId, canDelete returns false`() {
            assertFalse(
                ContentPermissions.canDelete(
                    ownerIdentityId = "",
                    currentIdentityId = "some-user",
                    role = GuestRole.GUEST,
                    legacyOwnerId = "different-legacy-id",
                )
            )
        }

        @Test
        fun `given blank ownerIdentityId and blank legacyOwnerId, canDelete returns false for non-admin`() {
            assertFalse(
                ContentPermissions.canDelete(
                    ownerIdentityId = "",
                    currentIdentityId = "some-user",
                    role = GuestRole.GUEST,
                    legacyOwnerId = "",
                )
            )
        }

        @Test
        fun `given ownerIdentityId set, legacyOwnerId is ignored`() {
            // ownerIdentityId does NOT match current, but legacyOwnerId does — should still deny
            assertFalse(
                ContentPermissions.canDelete(
                    ownerIdentityId = "real-owner",
                    currentIdentityId = "legacy-user",
                    role = GuestRole.GUEST,
                    legacyOwnerId = "legacy-user",
                )
            )
        }
    }

    // ── resolveOwnerId ────────────────────────────────────────────────────────

    @Nested
    inner class ResolveOwnerId {

        @Test
        fun `given both IDs set, returns ownerIdentityId`() {
            val result = ContentPermissions.resolveOwnerId(
                ownerIdentityId = "new-identity",
                ownerUserId = "old-user-id",
            )
            assert(result == "new-identity")
        }

        @Test
        fun `given blank ownerIdentityId, falls back to ownerUserId`() {
            val result = ContentPermissions.resolveOwnerId(
                ownerIdentityId = "",
                ownerUserId = "old-user-id",
            )
            assert(result == "old-user-id")
        }

        @Test
        fun `given both blank, returns empty string`() {
            val result = ContentPermissions.resolveOwnerId("", "")
            assert(result == "")
        }
    }

    // ── Legacy API backward compatibility ─────────────────────────────────────

    @Nested
    inner class LegacyApi {

        @Test
        fun `canEditPhoto returns true when currentUserId matches owner`() {
            assertTrue(ContentPermissions.canEditPhoto("uid-1", "uid-1"))
        }

        @Test
        fun `canEditPhoto returns false when currentUserId is null`() {
            assertFalse(ContentPermissions.canEditPhoto("uid-1", null))
        }

        @Test
        fun `canDeletePhoto returns true for admin even with different owner`() {
            assertTrue(ContentPermissions.canDeletePhoto("uid-1", "admin-uid", GuestRole.ADMIN))
        }

        @Test
        fun `canDeletePost returns true for coadmin even with different owner`() {
            assertTrue(ContentPermissions.canDeletePost("uid-1", "coadmin-uid", GuestRole.COADMIN))
        }

        @Test
        fun `canEditPost returns false when currentUserId is blank`() {
            assertFalse(ContentPermissions.canEditPost("uid-1", ""))
        }
    }
}
