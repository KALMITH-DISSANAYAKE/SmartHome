package com.example.smarthome.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthome.data.model.UsageLog
import com.example.smarthome.data.repository.UsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    usageRepository: UsageRepository
) : ViewModel() {

    val logs: StateFlow<List<UsageLog>> = usageRepository.getUsageLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats: StateFlow<Map<String, Int>> = logs.map { list ->
        list.groupingBy { it.deviceName }.eachCount()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
}