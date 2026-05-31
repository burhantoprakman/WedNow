package com.wednowapp.wednow.core.identity

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.wednowapp.wednow.core.session.GuestSessionManager
import com.wednowapp.wednow.domain.model.AuthProvider
import com.wednowapp.wednow.domain.model.AuthUser
import com.wednowapp.wednow.domain.model.Identity
import com.wednowapp.wednow.domain.model.IdentityType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton source-of-truth for the active [Identity].
 *
 * Every app installation starts with a GUEST identity whose [Identity.identityId]
 * is a device-local UUID (same value as the legacy [GuestSessionManager] guestId
 * for backward compatibility).  When Firebase Auth reports a sign-in the identity
 * is upgraded to USER and [IdentityMigrationService] is scheduled to reattach
 * all previously created content.
 *
 * Sign-out creates a fresh GUEST identity rather than restoring the old one —
 * this keeps browsing available while removing all privileged operations.
 *
 * ── Integration ──────────────────────────────────────────────────────────────
 * [AuthViewModel] must call [onSignIn] / [onSignOut] after the Firebase Auth
 * operation completes.  The internal FirebaseAuth listener provides a safety net
 * but cannot be the primary driver (it fires before the app has wired migration).
 */
@Singleton
class IdentityManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val migrationService: IdentityMigrationService,
) {

    // Background scope — outlives any single ViewModel
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _identity = MutableStateFlow(initIdentity())
    val identity: StateFlow<Identity> = _identity.asStateFlow()

    /** Current identity ID (UUID for GUEST, Firebase UID for USER). */
    val currentIdentityId: String get() = _identity.value.identityId

    /** True only for USER identities (Firebase-authenticated). */
    val isAuthenticated: Boolean get() = _identity.value.isAuthenticated

    /** The full current identity — use [currentIdentityId] for authoring, not display. */
    val currentIdentity: Identity get() = _identity.value

    // ── Auth state observation (safety net) ───────────────────────────────────
    init {
        firebaseAuth.addAuthStateListener { auth ->
            val user = auth.currentUser
            val current = _identity.value
            if (user != null && (current.type == IdentityType.GUEST || current.identityId != user.uid)) {
                // Firebase shows a signed-in user that we haven't handled yet
                val newIdentity = Identity(
                    identityId = user.uid,
                    type = IdentityType.USER,
                    provider = user.providerData
                        .firstOrNull { it.providerId != "firebase" }
                        ?.providerId?.toAuthProvider() ?: AuthProvider.NONE,
                    displayName = user.displayName,
                    email = user.email,
                    photoUrl = user.photoUrl?.toString(),
                    createdAt = current.createdAt,
                    linkedGuestId = current.guestIdToMigrate(),
                )
                applyUserIdentity(prev = current, next = newIdentity)
            } else if (user == null && current.type == IdentityType.USER) {
                // Firebase shows signed-out but we still hold a USER identity
                createAndApplyGuestIdentity()
            }
        }
    }

    // ── Explicit sign-in / sign-out (called from AuthViewModel) ──────────────

    /**
     * Upgrades the identity to USER and schedules a background content migration.
     * Safe to call more than once — idempotent when [user.uid] matches the
     * current identity.
     */
    fun onSignIn(user: AuthUser) {
        val current = _identity.value
        if (current.type == IdentityType.USER && current.identityId == user.uid) return
        val newIdentity = Identity(
            identityId = user.uid,
            type = IdentityType.USER,
            provider = user.provider.toAuthProvider(),
            displayName = user.displayName,
            email = user.email,
            photoUrl = user.photoUrl,
            createdAt = current.createdAt,
            linkedGuestId = current.guestIdToMigrate(),
        )
        applyUserIdentity(prev = current, next = newIdentity)
        // Update the legacy GuestSessionManager name field for old code paths
        if (!user.displayName.isNullOrBlank()) {
            GuestSessionManager.saveGuestName(context, user.displayName)
        }
    }

    /**
     * Downgrades to a fresh GUEST identity.
     *
     * The last-active wedding ID is intentionally preserved in [WeddingSessionManager]
     * so the user can quickly return after re-authentication.
     */
    fun onSignOut() {
        createAndApplyGuestIdentity()
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private fun initIdentity(): Identity {
        // 1. Try the dedicated IdentityPreferences store first
        IdentityPreferences.load(context)?.let { return it }

        // 2. Fall back to the legacy guestId so existing sessions are preserved
        val legacyGuestId = GuestSessionManager.getGuestId(context)
        val guest = Identity(
            identityId = legacyGuestId,
            type = IdentityType.GUEST,
            provider = AuthProvider.NONE,
        )
        IdentityPreferences.save(context, guest)
        return guest
    }

    private fun applyUserIdentity(prev: Identity, next: Identity) {
        _identity.value = next
        IdentityPreferences.save(context, next)

        val guestId = prev.identityId.takeIf { prev.type == IdentityType.GUEST }
        if (guestId != null) {
            scope.launch {
                runCatching {
                    migrationService.migrate(guestId = guestId, userIdentity = next)
                }
                // Migration failures are silent — content can be re-migrated
                // on the next sign-in if something went wrong.
            }
        }
    }

    private fun createAndApplyGuestIdentity() {
        val newGuestId = UUID.randomUUID().toString()
        val guest = Identity(
            identityId = newGuestId,
            type = IdentityType.GUEST,
            provider = AuthProvider.NONE,
        )
        _identity.value = guest
        IdentityPreferences.save(context, guest)
        // Keep the legacy GuestSessionManager in sync so older code paths work
        GuestSessionManager.saveGuestId(context, newGuestId)
    }

    // ── Utility extensions ────────────────────────────────────────────────────

    private fun Identity.guestIdToMigrate(): String? =
        identityId.takeIf { type == IdentityType.GUEST }

    private fun String.toAuthProvider(): AuthProvider = when {
        contains("google") -> AuthProvider.GOOGLE
        contains("apple") -> AuthProvider.APPLE
        else -> AuthProvider.NONE
    }
}
