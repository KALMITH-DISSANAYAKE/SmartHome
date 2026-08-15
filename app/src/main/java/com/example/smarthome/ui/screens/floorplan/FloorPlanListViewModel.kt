package com.example.smarthome.ui.screens.floorplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthome.data.model.FloorPlan
import com.example.smarthome.data.repository.DeviceRepository
import com.example.smarthome.data.repository.FloorPlanRepository
import com.example.smarthome.data.repository.UsageRepository
import com.example.smarthome.data.seeder.FirebaseSeeder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FloorPlanListViewModel @Inject constructor(
    private val repository: FloorPlanRepository,
    private val deviceRepository: DeviceRepository,
    private val usageRepository: UsageRepository,
    private val seeder: FirebaseSeeder
) : ViewModel() {

    val floorPlans: StateFlow<List<FloorPlan>> = repository.getFloorPlans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addFloorPlan(name: String) {
        viewModelScope.launch {
            repository.addFloorPlan(FloorPlan(name = name))
        }
    }

    fun deleteFloorPlan(id: String) {
        viewModelScope.launch {
            // Clean up devices first
            deviceRepository.deleteDevicesByFloorPlan(id)
            // Then delete the floor plan
            repository.deleteFloorPlan(id)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            deviceRepository.deleteAllDevices()
            usageRepository.deleteAllLogs()
            repository.deleteAllFloorPlans()
        }
    }

    fun seedDemoData() {
        viewModelScope.launch {
            seeder.seedDemoData()
        }
    }
}
