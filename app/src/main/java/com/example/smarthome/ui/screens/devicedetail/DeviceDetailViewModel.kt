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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import com.example.smarthome.data.model.DeviceType
import com.example.smarthome.util.AlarmScheduler

@HiltViewModel
class DeviceDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val deviceRepository: DeviceRepository,
    private val usageRepository: UsageRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val deviceId: String = savedStateHandle.get<String>("deviceId") ?: ""

    val device: StateFlow<Device?> = deviceRepository.getDeviceById(deviceId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val usageLogs: StateFlow<List<UsageLog>> = usageRepository.getUsageLogs()
        .map { logs -> logs.filter { it.deviceId == deviceId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadDevice(id: String) {
        // Device is already loaded via SavedStateHandle, but kept for compatibility
    }

    fun toggleDevice() {
        viewModelScope.launch {
            val current = device.value ?: return@launch
            deviceRepository.toggleDevice(current.id, current.status)
            
            if (current.type == DeviceType.IRON) {
                if (current.status == DeviceStatus.OFF) { // About to turn ON
                    AlarmScheduler.scheduleIronCutoff(context, current.id, current.name, current.maxOnDuration)
                } else { // About to turn OFF
                    AlarmScheduler.cancelIronCutoff(context, current.id)
                }
            }
        }
    }

    fun updateSchedule(onTime: String?, offTime: String?) {
        viewModelScope.launch {
            deviceRepository.updateDeviceField(deviceId, "scheduleOnTime", onTime ?: "")
            deviceRepository.updateDeviceField(deviceId, "scheduleOffTime", offTime ?: "")
            
            if (!onTime.isNullOrBlank()) {
                AlarmScheduler.scheduleLightAlarm(context, deviceId, onTime, true)
            }
            if (!offTime.isNullOrBlank()) {
                AlarmScheduler.scheduleLightAlarm(context, deviceId, offTime, false)
            }
            
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

    fun updateIronDuration(minutes: Int) {
        updateMaxDuration(minutes)
    }

    fun updateSwitch(index: Int, state: Boolean) {
        viewModelScope.launch {
            deviceRepository.updateSwitchState(deviceId, index, !state)
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