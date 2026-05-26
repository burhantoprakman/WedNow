package com.wednowapp.wednow.core.session

import android.content.Context
import java.util.UUID

object GuestSessionManager {

    private const val PREF_NAME = "wedding_app"
    private const val KEY_GUEST_ID = "guest_id"

    fun getGuestId(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val existing = prefs.getString(KEY_GUEST_ID, null)
        if (existing != null) return existing

        val newId = UUID.randomUUID().toString()
        prefs.edit().putString(KEY_GUEST_ID, newId).apply()

        return newId
    }
}
