package com.example.smarthome.ui.screens.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.smarthome.data.model.Device
import com.example.smarthome.data.model.FloorPlan

@Composable
fun FloorPlanGrid(
    floorPlan: FloorPlan,
    devices: List<Device>,
    onCellClick: (Int, Int) -> Unit,
    onDeviceToggle: (Device) -> Unit,
    onSwitchToggle: (String, Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val cellSize: Dp = floorPlan.cellSizeDp.dp
    val gridWidth = floorPlan.gridWidth
    val gridHeight = floorPlan.gridHeight

    val density = LocalDensity.current
    val cellSizePx = with(density) { cellSize.toPx() }
    val gridWidthPx = cellSizePx * gridWidth.toFloat()
    val gridHeightPx = cellSizePx * gridHeight.toFloat()

    Box(
        modifier = modifier
            .size(width = cellSize * gridWidth, height = cellSize * gridHeight)
            .pointerInput(gridWidth, gridHeight, cellSizePx) {
                detectTapGestures { offset ->
                    val x = (offset.x / cellSizePx).toInt()
                    val y = (offset.y / cellSizePx).toInt()
                    if (x in 0 until gridWidth && y in 0 until gridHeight) {
                        val deviceAtCell = devices.find { it.x == x && it.y == y }
                        if (deviceAtCell == null) {
                            onCellClick(x, y)
                        }
                    }
                }
            }
    ) {
        // Grid lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (i in 0..gridWidth) {
                val x = i * cellSizePx
                drawLine(
                    color = Color.LightGray,
                    start = Offset(x, 0f),
                    end = Offset(x, gridHeightPx),
                    strokeWidth = 1f
                )
            }
            for (i in 0..gridHeight) {
                val y = i * cellSizePx
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, y),
                    end = Offset(gridWidthPx, y),
                    strokeWidth = 1f
                )
            }
        }

        // Devices placed on grid
        devices.forEach { device ->
            DeviceOnGrid(
                device = device,
                cellSize = cellSize,
                onToggle = { onDeviceToggle(device) },
                onSwitchToggle = { switchIndex, state ->
                    onSwitchToggle(device.id, switchIndex, state)
                }
            )
        }
    }
}

@Composable
private fun DeviceOnGrid(
    device: Device,
    cellSize: Dp,
    onToggle: () -> Unit,
    onSwitchToggle: (Int, Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .offset(x = cellSize * device.x, y = cellSize * device.y)
            .size(cellSize)
    ) {
        DeviceCard(
            device = device,
            cellSize = cellSize,
            onToggle = onToggle,
            onSwitchToggle = onSwitchToggle,
            isGridView = true
        )
    }
}