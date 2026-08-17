package com.example.smarthome.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.smarthome.util.NotificationHelper
import com.google.firebase.firestore.FirebaseFirestore

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val deviceId = intent.getStringExtra("deviceId") ?: return
        val deviceName = intent.getStringExtra("deviceName") ?: "Device"
        val firestore = FirebaseFirestore.getInstance()

        Log.d("AlarmReceiver", "Received alarm for device $deviceId with action $action")

        when (action) {
            "ACTION_LIGHT_ON" -> {
                updateDeviceStatus(firestore, deviceId, "ON")
            }
            "ACTION_LIGHT_OFF" -> {
                updateDeviceStatus(firestore, deviceId, "OFF")
            }
            "ACTION_IRON_CUTOFF" -> {
                firestore.collection("devices").document(deviceId).get().addOnSuccessListener { doc ->
                    val status = doc.getString("status")
                    if (status == "ON") {
                        val now = System.currentTimeMillis()
                        
                        firestore.collection("devices").document(deviceId).update(
                            mapOf(
                                "status" to "OFF",
                                "currentSessionStart" to null,
                                "lastUpdated" to now
                            )
                        )
                        
                        // Log usage
                        val logRef = firestore.collection("usageLogs").document()
                        logRef.set(
                            mapOf(
                                "deviceId" to deviceId,
                                "deviceName" to deviceName,
                                "action" to "SAFETY_CUTOFF",
                                "timestamp" to now,
                                "details" to "Auto-turned OFF by App Exact Alarm"
                            )
                        )
                        
                        NotificationHelper.showNotification(
                            context = context,
                            title = "Safety Alert",
                            body = "$deviceName was auto-turned OFF after exceeding limit.",
                            deviceId = deviceId
                        )
                    }
                }
            }
        }
    }

    private fun updateDeviceStatus(firestore: FirebaseFirestore, deviceId: String, status: String) {
        firestore.collection("devices").document(deviceId).update(
            mapOf(
                "status" to status,
                "lastUpdated" to System.currentTimeMillis()
            )
        )
    }
}
