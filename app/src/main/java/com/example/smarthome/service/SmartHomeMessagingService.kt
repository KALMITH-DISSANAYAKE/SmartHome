package com.example.smarthome.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.smarthome.MainActivity
import com.example.smarthome.R
import com.example.smarthome.util.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SmartHomeMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: message.data["title"] ?: "Smart Home Alert"
        val body = message.notification?.body ?: message.data["body"] ?: "Device status changed"
        val deviceId = message.data["deviceId"]
        
        NotificationHelper.showNotification(this, title, body, deviceId)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Optional: store token in Firestore under users/{uid}/fcmToken
    }
}
