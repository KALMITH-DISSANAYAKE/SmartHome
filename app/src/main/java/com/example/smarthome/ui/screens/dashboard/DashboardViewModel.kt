package com.example.smarthome.ui.screens.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthome.data.model.Device
import com.example.smarthome.data.model.DeviceStatus
import com.example.smarthome.data.model.FloorPlan
import com.example.smarthome.data.model.UsageLog
import com.example.smarthome.data.repository.DeviceRepository
import com.example.smarthome.data.repository.FloorPlanRepository
import com.example.smarthome.data.repository.UsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val floorPlanRepository: FloorPlanRepository,
    private val deviceRepository: DeviceRepository,
    private val usageRepository: UsageRepository
) : ViewModel() {

    private val floorPlanId: String = savedStateHandle.get<String>("floorPlanId") ?: ""

    val floorPlan: StateFlow<FloorPlan?> = floorPlanRepository.getFloorPlans()
        .map { plans -> plans.find { it.id == floorPlanId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val devices: StateFlow<List<Device>> = deviceRepository.getDevicesByFloorPlan(floorPlanId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCell = MutableStateFlow<Pair<Int, Int>?>(null)
    val selectedCell: StateFlow<Pair<Int, Int>?> = _selectedCell.asStateFlow()

    fun selectCell(x: Int, y: Int) {
        _selectedCell.value = x to y
    }

    fun clearSelectedCell() {
        _selectedCell.value = null
    }

    fun addDevice(device: Device) {
        viewModelScope.launch {
            val newDevice = device.copy(floorPlanId = floorPlanId)
            deviceRepository.addDevice(newDevice)
            usageRepository.logUsage(
                UsageLog(
                    deviceId = newDevice.id,
                    deviceName = newDevice.name,
                    action = "ADDED",
                    details = "Added ${newDevice.type} at (${newDevice.x}, ${newDevice.y})"
                )
            )
        }
    }

    fun toggleDevice(device: Device) {
        viewModelScope.launch {
            deviceRepository.toggleDevice(device.id, device.status)
            val action = if (device.status == DeviceStatus.ON) "TURNED_OFF" else "TURNED_ON"
            usageRepository.logUsage(
                UsageLog(
                    deviceId = device.id,
                    deviceName = device.name,
                    action = action,
                    details = "Toggled from ${device.status}"
                )
            )
        }
    }

    fun toggleSwitch(deviceId: String, switchIndex: Int, currentState: Boolean) {
        viewModelScope.launch {
            deviceRepository.updateSwitchState(deviceId, switchIndex, !currentState)
        }
    }

    fun deleteDevice(deviceId: String) {
        viewModelScope.launch {
            deviceRepository.deleteDevice(deviceId)
        }
    }

    fun updateSchedule(deviceId: String, onTime: String?, offTime: String?) {
        viewModelScope.launch {
            deviceRepository.updateDeviceField(deviceId, "scheduleOnTime", onTime ?: "")
            deviceRepository.updateDeviceField(deviceId, "scheduleOffTime", offTime ?: "")
        }
    }
}