package com.example.smarthome.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSelector(
    label: String,
    time: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    
    // Parse initial time
    val initialHour = if (time.isNotBlank() && time.contains(":")) time.split(":")[0].toIntOrNull() ?: 12 else 12
    val initialMinute = if (time.isNotBlank() && time.contains(":")) time.split(":")[1].toIntOrNull() ?: 0 else 0
    
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    Box(modifier = modifier) {
        OutlinedTextField(
            value = time,
            onValueChange = { },
            label = { Text(label) },
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Default.AccessTime, contentDescription = "Select Time", tint = com.example.smarthome.ui.theme.PrimaryBlue)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = com.example.smarthome.ui.theme.PrimaryBlue,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedLabelColor = com.example.smarthome.ui.theme.PrimaryBlue
            )
        )
        
        // Transparent box over text field to intercept clicks reliably
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showDialog = true }
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val h = timePickerState.hour.toString().padStart(2, '0')
                    val m = timePickerState.minute.toString().padStart(2, '0')
                    onTimeSelected("$h:$m")
                    showDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DurationSelector(
    label: String,
    durationMinutes: Int,
    onDurationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    
    val displayHours = durationMinutes / 60
    val displayMins = durationMinutes % 60
    val displayText = if (displayHours > 0) "${displayHours}h ${displayMins}m" else "${displayMins}m"

    Box(modifier = modifier) {
        OutlinedTextField(
            value = displayText,
            onValueChange = { },
            label = { Text(label) },
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Default.Timer, contentDescription = "Select Duration", tint = com.example.smarthome.ui.theme.PrimaryBlue)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = com.example.smarthome.ui.theme.PrimaryBlue,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedLabelColor = com.example.smarthome.ui.theme.PrimaryBlue
            )
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showDialog = true }
        )
    }

    if (showDialog) {
        var hours by remember { mutableStateOf(displayHours.toString()) }
        var mins by remember { mutableStateOf(displayMins.toString()) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Duration") },
            text = {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = hours,
                        onValueChange = { hours = it },
                        label = { Text("Hours") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = mins,
                        onValueChange = { mins = it },
                        label = { Text("Minutes") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val h = hours.toIntOrNull() ?: 0
                    val m = mins.toIntOrNull() ?: 0
                    onDurationSelected(h * 60 + m)
                    showDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
