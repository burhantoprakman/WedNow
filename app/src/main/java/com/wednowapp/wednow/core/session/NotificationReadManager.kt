package com.wednowapp.wednow.core.session

import android.content.Context

object NotificationReadManager {

    private const val PREF_NAME = "wedding_app"
    private const val KEY_READ_IDS = "read_notification_ids"

    fun getReadIds(context: Context): Set<String> =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_READ_IDS, emptySet()) ?: emptySet()

    fun markAsRead(context: Context, notificationId: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val updated = (prefs.getStringSet(KEY_READ_IDS, emptySet()) ?: emptySet())
            .toMutableSet()
            .also { it.add(notificationId) }
        prefs.edit().putStringSet(KEY_READ_IDS, updated).apply()
    }
}
