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
    val density = LocalDensity.current
    val cellSizePx = with(density) { floorPlan.cellSizeDp.dp.roundToPx() }
    val gridWidthPx = cellSizePx * floorPlan.gridWidth
    val gridHeightPx = cellSizePx * floorPlan.gridHeight

    val cellSizeDp = with(density) { cellSizePx.toDp() }
    val gridWidthDp = cellSizeDp * floorPlan.gridWidth
    val gridHeightDp = cellSizeDp * floorPlan.gridHeight

    Box(
        modifier = modifier
            .size(width = gridWidthDp, height = gridHeightDp)
            .pointerInput(floorPlan.gridWidth, floorPlan.gridHeight, cellSizePx) {
                detectTapGestures { offset ->
                    val x = (offset.x / cellSizePx).toInt()
                    val y = (offset.y / cellSizePx).toInt()
                    if (x in 0 until floorPlan.gridWidth && y in 0 until floorPlan.gridHeight) {
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
            for (i in 0..floorPlan.gridWidth) {
                val x = (i * cellSizePx).toFloat()
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    start = Offset(x, 0f),
                    end = Offset(x, gridHeightPx.toFloat()),
                    strokeWidth = 1f // Precise 1 pixel stroke
                )
            }
            for (i in 0..floorPlan.gridHeight) {
                val y = (i * cellSizePx).toFloat()
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(gridWidthPx.toFloat(), y),
                    strokeWidth = 1f // Precise 1 pixel stroke
                )
            }
        }

        // Devices placed on grid
        devices.forEach { device ->
            DeviceOnGrid(
                device = device,
                cellSize = cellSizeDp,
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