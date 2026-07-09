package com.maritsa.app.core.session

import android.content.Context
import java.util.UUID

object GuestSessionManager {

    private const val PREF_NAME = "wedding_app"
    private const val KEY_GUEST_ID = "guest_id"
    private const val KEY_GUEST_NAME = "guest_name"

    fun getGuestId(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY_GUEST_ID, null)
        if (existing != null) return existing
        val newId = UUID.randomUUID().toString()
        prefs.edit().putString(KEY_GUEST_ID, newId).apply()
        return newId
    }

    /**
     * Replaces the local guestId.  Called by [LinkGuestToAuthUseCase] when a
     * previously-linked guestId is recovered from another device via Firebase Auth.
     */
    fun saveGuestId(context: Context, guestId: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_GUEST_ID, guestId).apply()
    }

    /** Called after a guest successfully joins (or creates) a wedding so the
     *  display name is available for photo uploads and other UI. */
    fun saveGuestName(context: Context, name: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_GUEST_NAME, name).apply()
    }

    /** Returns the stored display name, or an empty string if not yet set. */
    fun getGuestName(context: Context): String =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_GUEST_NAME, null).orEmpty()
}
