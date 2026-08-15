package com.example.smarthome.data.model

data class UsageLog(
    val id: String = "",
    val deviceId: String = "",
    val deviceName: String = "",
    val action: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val duration: Long = 0,
    val details: String = ""
)