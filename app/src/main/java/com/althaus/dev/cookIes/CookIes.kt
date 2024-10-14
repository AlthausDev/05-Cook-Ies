package com.althaus.dev.cookIes

import android.app.Application
import android.content.Context

class CookIes: Application() {
    companion object {
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = this
    }
}