package com.example.smarthome.ui.screens.floorplan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smarthome.data.model.FloorPlan
import com.example.smarthome.util.NotificationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloorPlanListScreen(
    viewModel: FloorPlanListViewModel = hiltViewModel(),
    onFloorPlanSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val floorPlans by viewModel.floorPlans.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Home Floor Plans") },
                actions = {
                    IconButton(onClick = { 
                        NotificationHelper.showNotification(context, "Test Alert", "Hardware notification is working!")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Test Notification"
                        )
                    }
                    IconButton(onClick = { showDeleteAllDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear All",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    IconButton(onClick = { viewModel.seedDemoData() }) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Seed Demo Data"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Floor Plan")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(floorPlans, key = { it.id }) { plan ->
                FloorPlanCard(
                    floorPlan = plan,
                    onClick = { onFloorPlanSelected(plan.id) },
                    onDelete = { viewModel.deleteFloorPlan(plan.id) }
                )
            }
        }

        if (showAddDialog) {
            AddFloorPlanDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name ->
                    viewModel.addFloorPlan(name)
                    showAddDialog = false
                }
            )
        }

        if (showDeleteAllDialog) {
            DeleteAllConfirmDialog(
                onDismiss = { showDeleteAllDialog = false },
                onConfirm = {
                    viewModel.clearAllData()
                    showDeleteAllDialog = false
                }
            )
        }
    }
}

@Composable
fun DeleteAllConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Factory Reset") },
        text = { Text("This will permanently delete all floor plans, devices, and usage logs. Are you sure?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete Everything", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloorPlanCard(
    floorPlan: FloorPlan,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = floorPlan.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Grid: ${floorPlan.gridWidth} x ${floorPlan.gridHeight}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AddFloorPlanDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Floor Plan") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Floor Plan Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
