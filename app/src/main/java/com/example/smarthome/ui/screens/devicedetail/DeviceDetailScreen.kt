package com.example.smarthome.ui.screens.devicedetail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smarthome.data.model.Device
import com.example.smarthome.data.model.DeviceStatus
import com.example.smarthome.data.model.DeviceType
import com.example.smarthome.ui.screens.dashboard.*
import com.example.smarthome.ui.components.TimeSelector
import com.example.smarthome.ui.components.DurationSelector
import com.example.smarthome.ui.theme.NeonGreen
import com.example.smarthome.ui.theme.PrimaryBlue

@Composable
fun DeviceDetailScreen(
    deviceId: String,
    viewModel: DeviceDetailViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val device by viewModel.device.collectAsState()
    val usageLogs by viewModel.usageLogs.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (device == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val d = device!!

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(d.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            val isOn = d.status == DeviceStatus.ON
            val hubColor by animateColorAsState(
                targetValue = if (isOn) NeonGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                animationSpec = tween(500)
            )
            val iconTint by animateColorAsState(
                targetValue = if (isOn) NeonGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(500)
            )

            Surface(
                shape = CircleShape,
                color = hubColor,
                modifier = Modifier
                    .size(140.dp)
                    .let { if (d.type != DeviceType.MULTI_SWITCH) it.clickable { viewModel.toggleDevice() } else it },
                border = BorderStroke(2.dp, if (isOn) NeonGreen.copy(alpha = 0.5f) else Color.Transparent)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    DeviceTypeIcon(type = d.type, tint = iconTint, size = 64.dp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isOn) "ACTIVE" else "OFFLINE",
                style = MaterialTheme.typography.titleMedium,
                color = if (isOn) NeonGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            
            Text(
                text = "${d.type.name.replace("_", " ")} • (${d.x}, ${d.y})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (d.powerRating > 0) {
                    PremiumCard {
                        ListItem(
                            headlineContent = { Text("Power Rating", fontWeight = FontWeight.SemiBold) },
                            trailingContent = { 
                                Text(
                                    text = "${d.powerRating} W",
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.Bold
                                ) 
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }

                when (d.type) {
                    DeviceType.MULTI_SWITCH -> {
                        MultiSwitchDetailCard(
                            switchStates = d.switchStates,
                            onToggle = { index, state -> viewModel.updateSwitch(index, state) }
                        )
                    }
                    DeviceType.IRON -> {
                        IronDetailCard(
                            device = d,
                            onUpdateDuration = { viewModel.updateIronDuration(it) }
                        )
                    }
                    DeviceType.LIGHT -> {
                        LightScheduleCard(
                            onTime = d.scheduleOnTime,
                            offTime = d.scheduleOffTime,
                            onUpdate = { on, off -> viewModel.updateSchedule(on, off) }
                        )
                    }
                    DeviceType.CAMERA -> {
                        CameraDetailCard(
                            uri = d.cameraMockUri,
                            onUpdateUri = { viewModel.updateCameraUri(it) }
                        )
                    }
                    else -> {}
                }

                if (d.powerRating > 0) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        ListItem(
                            headlineContent = { Text("Power Rating") },
                            trailingContent = { Text("${d.powerRating} W") }
                        )
                    }
                }

                // Last Updated
                Card(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text("Last Synced") },
                        trailingContent = {
                            Text(
                                java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                                    .format(java.util.Date(d.lastUpdated))
                            )
                        }
                    )
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete ${d.name}?") },
                text = { Text("This device will be permanently removed from the floor plan.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteDevice(onBack)
                            showDeleteDialog = false
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        content()
    }
}

@Composable
fun MultiSwitchDetailCard(
    switchStates: List<Boolean>,
    onToggle: (Int, Boolean) -> Unit
) {
    PremiumCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Switch Controls", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            MultiSwitchControls(switchStates = switchStates, onToggle = onToggle)
        }
    }
}

@Composable
fun IronDetailCard(
    device: Device,
    onUpdateDuration: (Int) -> Unit
) {
    PremiumCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Safety Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            if (device.status == DeviceStatus.ON) {
                IronSafetyDisplay(device = device)
                Spacer(modifier = Modifier.height(16.dp))
            }

            DurationSelector(
                label = "Max On Duration",
                durationMinutes = device.maxOnDuration,
                onDurationSelected = { onUpdateDuration(it) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun LightScheduleCard(
    onTime: String?,
    offTime: String?,
    onUpdate: (String?, String?) -> Unit
) {
    var onText by remember { mutableStateOf(onTime ?: "") }
    var offText by remember { mutableStateOf(offTime ?: "") }

    PremiumCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Auto Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    TimeSelector(
                        label = "Turn ON at",
                        time = onText,
                        onTimeSelected = { onText = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    TimeSelector(
                        label = "Turn OFF at",
                        time = offText,
                        onTimeSelected = { offText = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    onUpdate(
                        onText.takeIf { it.isNotBlank() },
                        offText.takeIf { it.isNotBlank() }
                    )
                },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Update Schedule", color = Color.White)
            }
        }
    }
}

@Composable
fun CameraDetailCard(
    uri: String?,
    onUpdateUri: (String) -> Unit
) {
    var uriText by remember { mutableStateOf(uri ?: "") }

    PremiumCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Camera Feed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                if (uri.isNullOrBlank()) {
                    Text("No feed URL configured", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    CameraMockView(uri = uri)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = uriText,
                onValueChange = { uriText = it },
                label = { Text("Mock Stream URI") },
                trailingIcon = {
                    IconButton(onClick = { onUpdateUri(uriText) }) {
                        Icon(Icons.Default.Save, "Save", tint = PrimaryBlue)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    focusedLabelColor = PrimaryBlue
                )
            )
        }
    }
}