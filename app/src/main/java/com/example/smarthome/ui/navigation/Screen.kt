package com.example.smarthome.ui.navigation

sealed class Screen(val route: String) {
    object FloorPlans : Screen("floor_plans")
    object Dashboard : Screen("dashboard/{floorPlanId}") {
        fun createRoute(floorPlanId: String) = "dashboard/$floorPlanId"
    }
    object DeviceDetail : Screen("device_detail/{deviceId}") {
        fun createRoute(deviceId: String) = "device_detail/$deviceId"
    }
    object Reports : Screen("reports")
}