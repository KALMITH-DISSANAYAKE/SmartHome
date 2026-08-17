package com.example.smarthome.ui.screens.devicedetail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smarthome.data.model.Device
import com.example.smarthome.data.model.DeviceStatus
import com.example.smarthome.data.model.DeviceType
import com.example.smarthome.ui.screens.dashboard.*
import com.example.smarthome.ui.components.TimeSelector
import com.example.smarthome.ui.components.DurationSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    viewModel: DeviceDetailViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    
    val device by viewModel.device.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    device?.let { d ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(d.name) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DeviceTypeIcon(d.type, MaterialTheme.colorScheme.primary, 48.dp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(d.name, style = MaterialTheme.typography.headlineSmall)
                                Text(
                                    "${d.type.name} • (${d.x}, ${d.y})",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        StatusBadge(d.status)
                    }
                }

                // Power Toggle
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Power State", style = MaterialTheme.typography.titleMedium)
                        Switch(
                            checked = d.status == DeviceStatus.ON,
                            onCheckedChange = { viewModel.toggleDevice() }
                        )
                    }
                }

                // Type-specific controls
                when (d.type) {
                    DeviceType.MULTI_SWITCH -> {
                        MultiSwitchDetailCard(
                            switchStates = d.switchStates,
                            onToggle = { idx, state ->
                                // handled via repository in real app; simplified here
                            }
                        )
                    }
                    DeviceType.IRON -> {
                        IronDetailCard(
                            device = d,
                            onUpdateDuration = { viewModel.updateMaxDuration(it) }
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

                // Power Info
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
    } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun MultiSwitchDetailCard(
    switchStates: List<Boolean>,
    onToggle: (Int, Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Switch Controls", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            MultiSwitchControls(switchStates = switchStates, onToggle = onToggle)
        }
    }
}

@Composable
fun IronDetailCard(
    device: Device,
    onUpdateDuration: (Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Safety Settings", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            if (device.status == DeviceStatus.ON) {
                IronSafetyDisplay(device = device)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                DurationSelector(
                    label = "Max On Duration",
                    durationMinutes = device.maxOnDuration,
                    onDurationSelected = { onUpdateDuration(it) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightScheduleCard(
    onTime: String?,
    offTime: String?,
    onUpdate: (String?, String?) -> Unit
) {
    var onText by remember { mutableStateOf(onTime ?: "") }
    var offText by remember { mutableStateOf(offTime ?: "") }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Auto Schedule", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

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

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    onUpdate(
                        onText.takeIf { it.isNotBlank() },
                        offText.takeIf { it.isNotBlank() }
                    )
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Update Schedule")
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

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Camera Feed", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            CameraMockView(uri = uri)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = uriText,
                onValueChange = { uriText = it },
                label = { Text("Mock Stream URI") },
                trailingIcon = {
                    IconButton(onClick = { onUpdateUri(uriText) }) {
                        Icon(Icons.Default.Save, "Save")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}