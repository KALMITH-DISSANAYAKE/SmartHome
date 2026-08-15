package com.example.smarthome

import android.app.Application
import com.example.smarthome.worker.WorkManagerScheduler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SmartHomeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        WorkManagerScheduler.scheduleLightWorker(this)
    }
}
