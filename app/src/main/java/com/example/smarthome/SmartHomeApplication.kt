package com.example.smarthome

import android.app.Application
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SmartHomeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Clear old scheduled background workers to prevent ClassNotFoundException 
        // since LightScheduleWorker was removed in favor of AlarmManager.
        WorkManager.getInstance(this).cancelAllWork()
    }
}
