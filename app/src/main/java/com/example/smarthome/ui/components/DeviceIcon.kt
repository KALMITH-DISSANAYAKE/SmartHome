package com.example.smarthome.ui.components

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info

@Composable
fun DeviceIcon(type: String) {
    Icon(imageVector = Icons.Default.Info, contentDescription = type)
}