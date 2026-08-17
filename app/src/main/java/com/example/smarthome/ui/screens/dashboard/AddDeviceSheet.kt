package com.example.smarthome.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.smarthome.data.model.Device
import com.example.smarthome.data.model.DeviceType
import com.example.smarthome.ui.components.TimeSelector
import com.example.smarthome.ui.components.DurationSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDeviceSheet(
    floorPlanId: String,
    initialX: Int,
    initialY: Int,
    onDismiss: () -> Unit,
    onAdd: (Device) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(DeviceType.OUTLET) }
    var x by remember { mutableStateOf(initialX.toString()) }
    var y by remember { mutableStateOf(initialY.toString()) }

    var switchCount by remember { mutableStateOf("2") }
    var maxDuration by remember { mutableStateOf(30) }
    var scheduleOn by remember { mutableStateOf("") }
    var scheduleOff by remember { mutableStateOf("") }
    var powerRating by remember { mutableStateOf("") }
    var cameraUri by remember { mutableStateOf("") }
    
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Add Device at ($initialX, $initialY)",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Device Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("Device Type", style = MaterialTheme.typography.labelLarge)
            DeviceTypeSelector(selected = selectedType, onSelect = { selectedType = it })

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = x,
                    onValueChange = { x = it },
                    label = { Text("X") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = y,
                    onValueChange = { y = it },
                    label = { Text("Y") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            when (selectedType) {
                DeviceType.MULTI_SWITCH -> {
                    OutlinedTextField(
                        value = switchCount,
                        onValueChange = { switchCount = it },
                        label = { Text("Number of Switches") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                DeviceType.IRON -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        DurationSelector(
                            label = "Max On Duration",
                            durationMinutes = maxDuration,
                            onDurationSelected = { maxDuration = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                DeviceType.LIGHT -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            TimeSelector(
                                label = "Auto ON (HH:mm)",
                                time = scheduleOn,
                                onTimeSelected = { scheduleOn = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            TimeSelector(
                                label = "Auto OFF (HH:mm)",
                                time = scheduleOff,
                                onTimeSelected = { scheduleOff = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                DeviceType.CAMERA -> {
                    OutlinedTextField(
                        value = cameraUri,
                        onValueChange = { cameraUri = it },
                        label = { Text("Mock Camera URI") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> {}
            }

            OutlinedTextField(
                value = powerRating,
                onValueChange = { 
                    powerRating = it
                    showError = false 
                },
                label = { Text("Power Rating (Watts)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = showError,
                modifier = Modifier.fillMaxWidth()
            )
            
            if (showError) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val power = powerRating.toDoubleOrNull() ?: 0.0
                        if (power <= 0.0) {
                            showError = true
                            errorMessage = "Power rating must be greater than 0"
                            return@Button
                        }
                        
                        val count = switchCount.toIntOrNull() ?: 2
                        val device = Device(
                            floorPlanId = floorPlanId,
                            name = name.ifBlank { "New ${selectedType.name}" },
                            type = selectedType,
                            x = x.toIntOrNull() ?: initialX,
                            y = y.toIntOrNull() ?: initialY,
                            switchCount = if (selectedType == DeviceType.MULTI_SWITCH) count else 1,
                            switchStates = if (selectedType == DeviceType.MULTI_SWITCH) List(count) { false } else emptyList(),
                            maxOnDuration = if (selectedType == DeviceType.IRON) maxDuration else 0,
                            scheduleOnTime = if (selectedType == DeviceType.LIGHT) scheduleOn.takeIf { it.isNotBlank() } else null,
                            scheduleOffTime = if (selectedType == DeviceType.LIGHT) scheduleOff.takeIf { it.isNotBlank() } else null,
                            cameraMockUri = if (selectedType == DeviceType.CAMERA) cameraUri.takeIf { it.isNotBlank() } else null,
                            powerRating = power
                        )
                        onAdd(device)
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Add Device")
                }
            }
        }
    }
}

@Composable
private fun DeviceTypeSelector(
    selected: DeviceType,
    onSelect: (DeviceType) -> Unit
) {
    val types = DeviceType.values()
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        types.forEach { type ->
            FilterChip(
                selected = type == selected,
                onClick = { onSelect(type) },
                label = { Text(type.name.replace("_", " ")) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}