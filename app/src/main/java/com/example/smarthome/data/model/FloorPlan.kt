package com.example.smarthome.data.model

data class FloorPlan(
    val id: String = "",
    val name: String = "",
    val gridWidth: Int = 20,
    val gridHeight: Int = 15,
    val cellSizeDp: Int = 24,
    val backgroundRes: String = "floor_sample" // reference name for demo assets
)