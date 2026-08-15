package com.example.smarthome.data.model

data class Device(
    val id: String = "",
    val floorPlanId: String = "",
    val name: String = "",
    val type: DeviceType = DeviceType.OUTLET,
    val status: DeviceStatus = DeviceStatus.OFF,
    val x: Int = 0,
    val y: Int = 0,

    // Multi-switch specific
    val switchCount: Int = 1,
    val switchStates: List<Boolean> = emptyList(),

    // Iron / Safety specific
    val maxOnDuration: Int = 1, // minutes
    val currentSessionStart: Long? = null,

    // Light scheduling
    val scheduleOnTime: String? = null, // "HH:mm"
    val scheduleOffTime: String? = null,

    // Camera
    val cameraMockUri: String? = null,

    // General
    val powerRating: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis()
)