package com.maritsa.app.core.session

import android.content.Context

object OnboardingManager {

    private const val PREF_NAME = "wedding_app"
    private const val KEY_ONBOARDING_DONE = "onboarding_done"

    fun isCompleted(context: Context): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ONBOARDING_DONE, false)

    fun markCompleted(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()
}
