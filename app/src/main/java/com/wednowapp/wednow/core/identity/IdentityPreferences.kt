package com.wednowapp.wednow.core.identity

import android.content.Context
import com.wednowapp.wednow.domain.model.AuthProvider
import com.wednowapp.wednow.domain.model.Identity
import com.wednowapp.wednow.domain.model.IdentityType

/**
 * Thin SharedPreferences wrapper that persists the active [Identity] locally.
 *
 * Writing to this store is the authoritative record of who the app thinks this
 * device belongs to.  Firestore is the cross-device truth; this is the fast,
 * offline-capable local cache.
 */
internal object IdentityPreferences {

    private const val PREF_NAME = "wednow_identity"
    private const val KEY_IDENTITY_ID = "identity_id"
    private const val KEY_TYPE = "identity_type"
    private const val KEY_PROVIDER = "auth_provider"
    private const val KEY_DISPLAY_NAME = "display_name"
    private const val KEY_EMAIL = "email"
    private const val KEY_PHOTO_URL = "photo_url"
    private const val KEY_CREATED_AT = "created_at"
    private const val KEY_LINKED_GUEST = "linked_guest_id"

    fun save(context: Context, identity: Identity) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_IDENTITY_ID, identity.identityId)
            .putString(KEY_TYPE, identity.type.name)
            .putString(KEY_PROVIDER, identity.provider.name)
            .putString(KEY_DISPLAY_NAME, identity.displayName)
            .putString(KEY_EMAIL, identity.email)
            .putString(KEY_PHOTO_URL, identity.photoUrl)
            .putLong(KEY_CREATED_AT, identity.createdAt)
            .putString(KEY_LINKED_GUEST, identity.linkedGuestId)
            .apply()
    }

    fun load(context: Context): Identity? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val id = prefs.getString(KEY_IDENTITY_ID, null) ?: return null
        return runCatching {
            Identity(
                identityId = id,
                type = IdentityType.valueOf(
                    prefs.getString(KEY_TYPE, null) ?: IdentityType.GUEST.name
                ),
                provider = AuthProvider.valueOf(
                    prefs.getString(KEY_PROVIDER, null) ?: AuthProvider.NONE.name
                ),
                displayName = prefs.getString(KEY_DISPLAY_NAME, null),
                email = prefs.getString(KEY_EMAIL, null),
                photoUrl = prefs.getString(KEY_PHOTO_URL, null),
                createdAt = prefs.getLong(KEY_CREATED_AT, System.currentTimeMillis()),
                linkedGuestId = prefs.getString(KEY_LINKED_GUEST, null),
            )
        }.getOrNull()
    }

    /** Fast path — returns only the stored identity ID without constructing the full object. */
    fun getIdentityId(context: Context): String? =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_IDENTITY_ID, null)

    fun clear(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply()
}
