package com.wednowapp.wednow.core.session

import android.content.Context

object WeddingSessionManager {

    private const val PREF_NAME = "wedding_app"
    private const val KEY_WEDDING_ID = "wedding_id"

    fun getWeddingId(context: Context): String? =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_WEDDING_ID, null)

    fun saveWeddingId(context: Context, weddingId: String) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_WEDDING_ID, weddingId).apply()

    fun clearWeddingId(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().remove(KEY_WEDDING_ID).apply()
}
