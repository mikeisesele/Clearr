package com.mikeisesele.clearr

import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ClearrApplication : android.app.Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }

    companion object {
        lateinit var appContext: android.content.Context
            private set
    }
}
