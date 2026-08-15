package com.example.smarthome.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun StatusBadge(status: String) {
    Text(text = "Status: $status")
}