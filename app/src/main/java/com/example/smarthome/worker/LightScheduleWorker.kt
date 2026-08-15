package com.example.smarthome.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.smarthome.data.model.DeviceStatus
import com.example.smarthome.util.TimeUtils
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class LightScheduleWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val firestore = Firebase.firestore

    override suspend fun doWork(): Result {
        return try {
            val devices = firestore.collection("devices")
                .get()
                .await()

            val now = System.currentTimeMillis()
            val batch = firestore.batch()
            var changes = 0

            devices.documents.forEach { doc ->
                val type = doc.getString("type")
                val currentStatus = doc.getString("status") ?: "OFF"

                // 1. Light Scheduling
                if (type == "LIGHT") {
                    val onTime = doc.getString("scheduleOnTime")
                    val offTime = doc.getString("scheduleOffTime")
                    if (!onTime.isNullOrBlank() && !offTime.isNullOrBlank()) {
                        val shouldBeOn = TimeUtils.isCurrentTimeInRange(onTime, offTime)
                        val desired = if (shouldBeOn) "ON" else "OFF"
                        if (currentStatus != desired) {
                            batch.update(doc.reference, mapOf("status" to desired, "lastUpdated" to now))
                            changes++
                        }
                    }
                }

                // 2. Iron Safety Backup (Every 15 min check)
                if (type == "IRON" && currentStatus == "ON") {
                    val start = doc.getLong("currentSessionStart") ?: now
                    val max = doc.getLong("maxOnDuration") ?: 30
                    val elapsedMin = (now - start) / 60000
                    if (elapsedMin >= max) {
                        batch.update(doc.reference, mapOf("status" to "OFF", "currentSessionStart" to null, "lastUpdated" to now))
                        // Also log usage
                        val logRef = firestore.collection("usageLogs").document()
                        batch.set(logRef, mapOf(
                            "deviceId" to doc.id,
                            "deviceName" to (doc.getString("name") ?: "Iron"),
                            "action" to "SAFETY_CUTOFF",
                            "timestamp" to now,
                            "details" to "Auto-turned OFF by App Worker after ${elapsedMin} min"
                        ))
                        changes++
                    }
                }
            }

            if (changes > 0) batch.commit().await()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
