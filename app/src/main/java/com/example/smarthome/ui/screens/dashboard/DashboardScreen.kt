package com.example.smarthome.ui.screens.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smarthome.data.model.Device
import com.example.smarthome.data.model.DeviceStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    floorPlanId: String,
    viewModel: DashboardViewModel = hiltViewModel(),
    onDeviceClick: (String) -> Unit,
    onBack: () -> Unit,
    onNavigateToReports: () -> Unit
) {
    BackHandler { onBack() }
    
    val floorPlan by viewModel.floorPlan.collectAsState()
    val devices by viewModel.devices.collectAsState()
    val selectedCell by viewModel.selectedCell.collectAsState()

    var showAddSheet by remember { mutableStateOf(false) }
    var deviceToDelete by remember { mutableStateOf<Device?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(floorPlan?.name ?: "Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToReports) {
                        Icon(Icons.Default.BarChart, "Reports")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Grid Section
            floorPlan?.let { plan ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.2f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    FloorPlanGrid(
                        floorPlan = plan,
                        devices = devices,
                        onCellClick = { x, y ->
                            viewModel.selectCell(x, y)
                            showAddSheet = true
                        },
                        onDeviceToggle = { device ->
                            viewModel.toggleDevice(device)
                        },
                        onSwitchToggle = { deviceId, switchIndex, state ->
                            viewModel.toggleSwitch(deviceId, switchIndex, state)
                        }
                    )
                }
            }

            // Device List Section
            Text(
                text = "Devices (${devices.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(devices, key = { it.id }) { device ->
                    DeviceListItem(
                        device = device,
                        onToggle = { viewModel.toggleDevice(device) },
                        onDelete = { deviceToDelete = device },
                        onClick = { onDeviceClick(device.id) }
                    )
                }
            }
        }

        val cell = selectedCell
        if (showAddSheet && cell != null) {
            AddDeviceSheet(
                floorPlanId = floorPlanId,
                initialX = cell.first,
                initialY = cell.second,
                onDismiss = {
                    showAddSheet = false
                    viewModel.clearSelectedCell()
                },
                onAdd = { device ->
                    viewModel.addDevice(device)
                    showAddSheet = false
                    viewModel.clearSelectedCell()
                }
            )
        }

        if (deviceToDelete != null) {
            AlertDialog(
                onDismissRequest = { deviceToDelete = null },
                title = { Text("Delete Device?") },
                text = { Text("Remove ${deviceToDelete?.name}?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            deviceToDelete?.let { viewModel.deleteDevice(it.id) }
                            deviceToDelete = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deviceToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun DeviceListItem(
    device: Device,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onClick() }
            ) {
                DeviceTypeIcon(
                    type = device.type,
                    tint = MaterialTheme.colorScheme.primary,
                    size = 28.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${device.type.name} • (${device.x}, ${device.y})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = device.status == DeviceStatus.ON,
                    onCheckedChange = { onToggle() }
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}