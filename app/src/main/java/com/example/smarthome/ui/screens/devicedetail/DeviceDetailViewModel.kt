package com.example.smarthome.ui.screens.devicedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthome.data.model.Device
import com.example.smarthome.data.model.DeviceStatus
import com.example.smarthome.data.model.UsageLog
import com.example.smarthome.data.repository.DeviceRepository
import com.example.smarthome.data.repository.UsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val deviceRepository: DeviceRepository,
    private val usageRepository: UsageRepository
) : ViewModel() {

    private val deviceId: String = savedStateHandle.get<String>("deviceId") ?: ""

    val device: StateFlow<Device?> = deviceRepository.getDeviceById(deviceId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun toggleDevice() {
        viewModelScope.launch {
            val current = device.value ?: return@launch
            deviceRepository.toggleDevice(current.id, current.status)
        }
    }

    fun updateSchedule(onTime: String?, offTime: String?) {
        viewModelScope.launch {
            deviceRepository.updateDeviceField(deviceId, "scheduleOnTime", onTime ?: "")
            deviceRepository.updateDeviceField(deviceId, "scheduleOffTime", offTime ?: "")
            usageRepository.logUsage(
                UsageLog(
                    deviceId = deviceId,
                    deviceName = device.value?.name ?: "",
                    action = "SCHEDULE_UPDATED",
                    details = "ON: $onTime, OFF: $offTime"
                )
            )
        }
    }

    fun updateMaxDuration(minutes: Int) {
        viewModelScope.launch {
            deviceRepository.updateDeviceField(deviceId, "maxOnDuration", minutes)
        }
    }

    fun updateCameraUri(uri: String) {
        viewModelScope.launch {
            deviceRepository.updateDeviceField(deviceId, "cameraMockUri", uri)
        }
    }

    fun deleteDevice(onDeleted: () -> Unit) {
        viewModelScope.launch {
            deviceRepository.deleteDevice(deviceId)
            onDeleted()
        }
    }
}