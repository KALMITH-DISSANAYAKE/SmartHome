package com.example.smarthome.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smarthome.ui.screens.dashboard.DashboardScreen
import com.example.smarthome.ui.screens.devicedetail.DeviceDetailScreen
import com.example.smarthome.ui.screens.floorplan.FloorPlanListScreen
import com.example.smarthome.ui.screens.reports.ReportsScreen

@Composable
fun SmartHomeNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.FloorPlans.route) {
        composable(Screen.FloorPlans.route) {
            FloorPlanListScreen(
                onFloorPlanSelected = { floorPlanId ->
                    navController.navigate(Screen.Dashboard.createRoute(floorPlanId))
                }
            )
        }
        composable(
            route = Screen.Dashboard.route,
            arguments = listOf(navArgument("floorPlanId") { type = NavType.StringType })
        ) { backStackEntry ->
            val floorPlanId = backStackEntry.arguments?.getString("floorPlanId") ?: ""
            DashboardScreen(
                floorPlanId = floorPlanId,
                onDeviceClick = { deviceId ->
                    navController.navigate(Screen.DeviceDetail.createRoute(deviceId))
                },
                onBack = { navController.popBackStack() },
                onNavigateToReports = {
                    navController.navigate(Screen.Reports.route)
                }
            )
        }
        composable(
            route = Screen.DeviceDetail.route,
            arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
            DeviceDetailScreen(
                deviceId = deviceId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Reports.route) {
            ReportsScreen(onBack = { navController.popBackStack() })
        }
    }
}
