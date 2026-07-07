package com.wednowapp.wednow

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class WedNowApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        // In release builds, plant a crash-reporting tree (e.g. FirebaseCrashlytics) here.
    }
}
