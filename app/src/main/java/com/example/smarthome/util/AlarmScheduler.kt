package com.example.smarthome.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.smarthome.receiver.AlarmReceiver
import java.util.*

object AlarmScheduler {

    fun scheduleLightAlarm(context: Context, deviceId: String, timeStr: String, isTurnOn: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val timeParts = timeStr.split(":")
        if (timeParts.size != 2) return
        val hours = timeParts[0].toIntOrNull() ?: return
        val minutes = timeParts[1].toIntOrNull() ?: return

        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Colombo")).apply {
            set(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.MINUTE, minutes)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If time is already passed today, schedule for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val action = if (isTurnOn) "ACTION_LIGHT_ON" else "ACTION_LIGHT_OFF"
        val requestCode = (deviceId.hashCode() + if (isTurnOn) 1 else 0)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = action
            putExtra("deviceId", deviceId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
            Log.d("AlarmScheduler", "Scheduled $action for $deviceId at ${calendar.time}")
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "Missing SCHEDULE_EXACT_ALARM permission", e)
        }
    }

    fun scheduleIronCutoff(context: Context, deviceId: String, deviceName: String, durationMinutes: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val triggerTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
        val requestCode = deviceId.hashCode()

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "ACTION_IRON_CUTOFF"
            putExtra("deviceId", deviceId)
            putExtra("deviceName", deviceName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
            Log.d("AlarmScheduler", "Scheduled ACTION_IRON_CUTOFF for $deviceId in $durationMinutes minutes")
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "Missing SCHEDULE_EXACT_ALARM permission", e)
        }
    }
    
    fun cancelIronCutoff(context: Context, deviceId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val requestCode = deviceId.hashCode()

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "ACTION_IRON_CUTOFF"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            Log.d("AlarmScheduler", "Cancelled ACTION_IRON_CUTOFF for $deviceId")
        }
    }
}
