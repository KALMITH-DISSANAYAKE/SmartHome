package com.example.smarthome.ui.screens.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smarthome.data.model.Device
import com.example.smarthome.data.model.DeviceStatus
import com.example.smarthome.data.model.DeviceType
import com.example.smarthome.ui.theme.NeonGreen
import com.example.smarthome.ui.theme.AccentPurple

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
                title = { 
                    Text(
                        text = floorPlan?.name ?: "Dashboard",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToReports) {
                        Icon(Icons.Default.BarChart, "Reports", tint = MaterialTheme.colorScheme.onBackground)
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
        ) {
            // Grid Section
            floorPlan?.let { plan ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.2f)
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState())
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Connected Devices",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = "${devices.size} Total",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
    val isOn = device.status == DeviceStatus.ON
    
    val borderColor by animateColorAsState(
        targetValue = if (isOn) NeonGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(400)
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isOn) NeonGreen.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface,
        animationSpec = tween(400)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isOn) NeonGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                DeviceTypeIcon(
                    type = device.type,
                    tint = if (isOn) NeonGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    size = 24.dp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${device.type.name.replace("_", " ")} • ${device.powerRating}W",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (device.type != DeviceType.MULTI_SWITCH) {
                    Switch(
                        checked = isOn,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NeonGreen,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}