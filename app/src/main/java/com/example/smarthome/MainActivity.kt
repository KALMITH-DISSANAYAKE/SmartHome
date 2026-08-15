package com.example.smarthome

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.smarthome.ui.navigation.SmartHomeNavGraph
import com.example.smarthome.ui.theme.SmartHomeTheme
import com.example.smarthome.util.NotificationHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request Notification Permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}.launch(
                android.Manifest.permission.POST_NOTIFICATIONS
            )
        }
        
        // Safety Alert Listener: Watch for system cutoffs
        listenForSafetyAlerts()

        setContent {
            SmartHomeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SmartHomeNavGraph()
                }
            }
        }
    }

    private fun listenForSafetyAlerts() {
        val startTime = System.currentTimeMillis()
        firestore.collection("usageLogs")
            .whereEqualTo("action", "SAFETY_CUTOFF")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, _ ->
                val log = snapshot?.documents?.firstOrNull()?.data
                val timestamp = log?.get("timestamp") as? Long ?: 0L
                
                // Only notify for NEW logs created after the app started
                if (timestamp > startTime) {
                    val deviceName = log?.get("deviceName") as? String ?: "Device"
                    NotificationHelper.showNotification(
                        context = this,
                        title = "Safety Alert",
                        body = "$deviceName was automatically turned OFF for safety.",
                        deviceId = log?.get("deviceId") as? String
                    )
                }
            }
    }
}
