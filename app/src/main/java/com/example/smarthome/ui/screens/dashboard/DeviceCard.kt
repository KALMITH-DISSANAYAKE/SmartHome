package com.example.smarthome.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.smarthome.data.model.Device
import com.example.smarthome.data.model.DeviceStatus
import com.example.smarthome.data.model.DeviceType

@Composable
fun DeviceCard(
    device: Device,
    cellSize: Dp = 48.dp,
    onToggle: () -> Unit,
    onSwitchToggle: (Int, Boolean) -> Unit,
    isGridView: Boolean = false,
    modifier: Modifier = Modifier
) {
    val statusColor = when (device.status) {
        DeviceStatus.ON -> Color(0xFF4CAF50)
        DeviceStatus.OFF -> Color(0xFF9E9E9E)
        DeviceStatus.ERROR -> Color(0xFFF44336)
        DeviceStatus.DISCONNECTED -> Color(0xFFFF9800)
    }

    if (isGridView) {
        Box(
            modifier = modifier
                .size(cellSize)
                .border(1.5.dp, statusColor, RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp))
                .background(statusColor.copy(alpha = 0.15f))
                .let { if (device.type != DeviceType.MULTI_SWITCH) it.clickable { onToggle() } else it },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(2.dp)
            ) {
                DeviceTypeIcon(
                    type = device.type,
                    tint = statusColor,
                    size = (cellSize.value * 0.4).dp
                )

                when {
                    device.type == DeviceType.MULTI_SWITCH && device.switchCount > 1 -> {
                        Text(
                            text = "${device.switchStates.count { it }}/${device.switchCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            textAlign = TextAlign.Center
                        )
                    }
                    device.type == DeviceType.IRON && device.status == DeviceStatus.ON -> {
                        val elapsed = device.currentSessionStart?.let {
                            ((System.currentTimeMillis() - it) / 60000).toInt()
                        } ?: 0
                        Text(
                            text = "${elapsed}m",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (elapsed >= device.maxOnDuration) Color.Red else statusColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .size(5.dp)
                    .background(statusColor, CircleShape)
            )
        }
    } else {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .let { if (device.type != DeviceType.MULTI_SWITCH) it.clickable { onToggle() } else it },
            colors = CardDefaults.cardColors(
                containerColor = statusColor.copy(alpha = 0.08f)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, statusColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DeviceTypeIcon(device.type, statusColor, 32.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = device.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Pos: (${device.x}, ${device.y})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    StatusBadge(device.status)
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (device.type) {
                    DeviceType.MULTI_SWITCH -> {
                        MultiSwitchControls(
                            switchStates = device.switchStates,
                            onToggle = onSwitchToggle
                        )
                    }
                    DeviceType.IRON -> {
                        IronSafetyDisplay(device = device)
                    }
                    DeviceType.LIGHT -> {
                        if (!device.scheduleOnTime.isNullOrBlank() || !device.scheduleOffTime.isNullOrBlank()) {
                            Text(
                                text = "Schedule: ${device.scheduleOnTime ?: "--"} → ${device.scheduleOffTime ?: "--"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    DeviceType.CAMERA -> {
                        CameraMockView(uri = device.cameraMockUri)
                    }
                    else -> {}
                }

                if (device.powerRating > 0) {
                    Text(
                        text = "Power: ${device.powerRating}W",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceTypeIcon(type: DeviceType, tint: Color, size: Dp) {
    val icon = when (type) {
        DeviceType.OUTLET -> Icons.Default.Power
        DeviceType.MULTI_SWITCH -> Icons.Default.ToggleOn
        DeviceType.LIGHT -> Icons.Default.Lightbulb
        DeviceType.IRON -> Icons.Default.Whatshot
        DeviceType.CAMERA -> Icons.Default.Videocam
    }
    Icon(
        imageVector = icon,
        contentDescription = type.name,
        tint = tint,
        modifier = Modifier.size(size)
    )
}

@Composable
fun StatusBadge(status: DeviceStatus) {
    val (color, text) = when (status) {
        DeviceStatus.ON -> Color(0xFF4CAF50) to "ON"
        DeviceStatus.OFF -> Color(0xFF9E9E9E) to "OFF"
        DeviceStatus.ERROR -> Color(0xFFF44336) to "ERROR"
        DeviceStatus.DISCONNECTED -> Color(0xFFFF9800) to "DISCONNECTED"
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        contentColor = color,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun MultiSwitchControls(
    switchStates: List<Boolean>,
    onToggle: (Int, Boolean) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        switchStates.forEachIndexed { index, state ->
            FilterChip(
                selected = state,
                onClick = { onToggle(index, state) },
                label = { Text("S${index + 1}") },
                leadingIcon = if (state) {
                    { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                } else null
            )
        }
    }
}

@Composable
fun IronSafetyDisplay(device: Device) {
    val elapsed = device.currentSessionStart?.let {
        ((System.currentTimeMillis() - it) / 60000).toInt()
    } ?: 0
    val remaining = device.maxOnDuration - elapsed
    val isOverLimit = remaining <= 0

    Column {
        LinearProgressIndicator(
            progress = { (elapsed.toFloat() / device.maxOnDuration).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
            color = if (isOverLimit) Color.Red else MaterialTheme.colorScheme.primary,
        )
        Text(
            text = if (isOverLimit) "SAFETY CUTOFF TRIGGERED!" else "$remaining min remaining (max ${device.maxOnDuration} min)",
            style = MaterialTheme.typography.bodySmall,
            color = if (isOverLimit) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CameraMockView(uri: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.DarkGray),
        contentAlignment = Alignment.Center
    ) {
        if (uri != null) {
            Text(
                text = "Camera Feed\n$uri",
                color = Color.White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "No Signal",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}