package com.example.smartwaste_user

import android.app.Application
import com.google.firebase.auth.FirebaseAuth

import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration

@HiltAndroidApp
class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

    }
}