package com.example.smarthome.data.seeder

import com.example.smarthome.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseSeeder @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun seedDemoData() {
        // Floor Plans
        val floor1 = FloorPlan(name = "Ground Floor", gridWidth = 16, gridHeight = 12, cellSizeDp = 28)
        val floor2 = FloorPlan(name = "First Floor", gridWidth = 14, gridHeight = 10, cellSizeDp = 32)

        val f1 = firestore.collection("floorPlans").add(floor1).await()
        val f2 = firestore.collection("floorPlans").add(floor2).await()

        val devices = listOf(
            // Ground Floor
            Device(floorPlanId = f1.id, name = "Living Room Light", type = DeviceType.LIGHT,
                x = 3, y = 2, status = DeviceStatus.OFF,
                scheduleOnTime = "18:00", scheduleOffTime = "23:00", powerRating = 60.0),
            Device(floorPlanId = f1.id, name = "Kitchen Outlet", type = DeviceType.OUTLET,
                x = 8, y = 4, status = DeviceStatus.OFF, powerRating = 1500.0),
            Device(floorPlanId = f1.id, name = "Clothing Iron", type = DeviceType.IRON,
                x = 10, y = 5, status = DeviceStatus.OFF, maxOnDuration = 1, powerRating = 2200.0),
            Device(floorPlanId = f1.id, name = "Entrance Cam", type = DeviceType.CAMERA,
                x = 1, y = 1, status = DeviceStatus.ON,
                cameraMockUri = "rtsp://demo-cam-01/live", powerRating = 5.0),
            Device(floorPlanId = f1.id, name = "Hall Switch", type = DeviceType.MULTI_SWITCH,
                x = 5, y = 3, status = DeviceStatus.OFF, switchCount = 3,
                switchStates = listOf(false, true, false)),

            // First Floor
            Device(floorPlanId = f2.id, name = "Bedroom Light", type = DeviceType.LIGHT,
                x = 4, y = 3, status = DeviceStatus.OFF, powerRating = 40.0),
            Device(floorPlanId = f2.id, name = "Bedroom Outlet", type = DeviceType.OUTLET,
                x = 7, y = 2, status = DeviceStatus.OFF, powerRating = 100.0)
        )

        devices.forEach { firestore.collection("devices").add(it).await() }
    }
}
