package com.example.smarthome

import android.os.Build
import android.os.Bundle
import android.util.Log
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
        
        Log.d("SMARTHOME_DEBUG", "MainActivity Created - Initializing Safety Listener")

        // Request Notification Permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                Log.d("SMARTHOME_DEBUG", "Notification permission granted: $granted")
            }.launch(
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
        Log.d("SMARTHOME_DEBUG", "Listening for logs after: $startTime")
        
        firestore.collection("usageLogs")
            .whereEqualTo("action", "SAFETY_CUTOFF")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("SMARTHOME_DEBUG", "Listen failed.", error)
                    return@addSnapshotListener
                }

                val doc = snapshot?.documents?.firstOrNull()
                if (doc != null) {
                    val log = doc.data
                    val timestamp = log?.get("timestamp") as? Long ?: 0L
                    Log.d("SMARTHOME_DEBUG", "New SAFETY_CUTOFF log detected at $timestamp")
                    
                    // Only notify for NEW logs created after the app started
                    if (timestamp > startTime) {
                        val deviceName = log?.get("deviceName") as? String ?: "Device"
                        Log.d("SMARTHOME_DEBUG", "TRIGGERING NOTIFICATION for $deviceName")
                        NotificationHelper.showNotification(
                            context = this,
                            title = "Safety Alert",
                            body = "$deviceName was automatically turned OFF for safety.",
                            deviceId = log?.get("deviceId") as? String
                        )
                    } else {
                        Log.d("SMARTHOME_DEBUG", "Log skipped (too old: $timestamp <= $startTime)")
                    }
                }
            }
    }
}
