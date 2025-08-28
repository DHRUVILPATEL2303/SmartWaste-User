package com.example.smartwaste_user

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration


@HiltAndroidApp
class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Configuration.getInstance().userAgentValue = "smartwaste-user-android"
    }
}