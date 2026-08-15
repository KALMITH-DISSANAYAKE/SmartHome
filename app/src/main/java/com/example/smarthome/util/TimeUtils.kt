package com.example.smarthome.util

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun parseTime(timeStr: String): Date? {
        return try {
            timeFormat.parse(timeStr)
        } catch (e: Exception) {
            null
        }
    }

    fun isCurrentTimeInRange(startTime: String?, endTime: String?): Boolean {
        if (startTime.isNullOrBlank() || endTime.isNullOrBlank()) return false

        val now = Calendar.getInstance()
        val start = parseTime(startTime)?.let {
            Calendar.getInstance().apply {
                time = it
                set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
            }
        } ?: return false

        val end = parseTime(endTime)?.let {
            Calendar.getInstance().apply {
                time = it
                set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
            }
        } ?: return false

        return now.timeInMillis in start.timeInMillis..end.timeInMillis
    }

    fun formatDuration(millis: Long): String {
        val minutes = millis / 60000
        val hours = minutes / 60
        val mins = minutes % 60
        return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
    }
}