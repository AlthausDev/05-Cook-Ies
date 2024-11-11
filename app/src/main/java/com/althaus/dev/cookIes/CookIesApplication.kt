package com.althaus.dev.cookIes

import android.app.Application
import android.content.Intent
import android.util.Log
import com.google.android.gms.security.ProviderInstaller
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CookIesApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        ProviderInstaller.installIfNeeded(applicationContext)
    }
}
