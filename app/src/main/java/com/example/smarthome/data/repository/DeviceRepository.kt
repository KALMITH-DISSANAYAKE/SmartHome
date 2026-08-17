package com.example.smarthome.data.repository

import com.example.smarthome.data.model.Device
import com.example.smarthome.data.model.DeviceStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getDevicesByFloorPlan(floorPlanId: String): Flow<List<Device>> = callbackFlow {
        val listener = firestore.collection("devices")
            .whereEqualTo("floorPlanId", floorPlanId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val devices = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Device::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(devices)
            }
        awaitClose { listener.remove() }
    }

    fun getAllDevices(): Flow<List<Device>> = callbackFlow {
        val listener = firestore.collection("devices")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val devices = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Device::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(devices)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addDevice(device: Device): String {
        return firestore.collection("devices").add(device).await().id
    }

    suspend fun deleteDevice(deviceId: String) {
        firestore.collection("devices").document(deviceId).delete().await()
    }

    suspend fun deleteDevicesByFloorPlan(floorPlanId: String) {
        val snapshot = firestore.collection("devices")
            .whereEqualTo("floorPlanId", floorPlanId)
            .get()
            .await()
        
        val batch = firestore.batch()
        snapshot.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }

    suspend fun deleteAllDevices() {
        val snapshot = firestore.collection("devices").get().await()
        val batch = firestore.batch()
        snapshot.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }

    suspend fun toggleDevice(deviceId: String, currentStatus: DeviceStatus) {
        val newStatus = if (currentStatus == DeviceStatus.ON) DeviceStatus.OFF else DeviceStatus.ON
        val updates = hashMapOf<String, Any?>(
            "status" to newStatus.name,
            "lastUpdated" to System.currentTimeMillis()
        )
        if (newStatus == DeviceStatus.ON) {
            updates["currentSessionStart"] = System.currentTimeMillis()
        } else {
            updates["currentSessionStart"] = null
        }
        firestore.collection("devices").document(deviceId).update(updates).await()
    }

    suspend fun updateSwitchState(deviceId: String, switchIndex: Int, state: Boolean) {
        val doc = firestore.collection("devices").document(deviceId).get().await()
        val states = (doc.get("switchStates") as? List<*>)?.map { it as? Boolean ?: false }?.toMutableList() ?: return
        if (switchIndex < states.size) {
            states[switchIndex] = state
            
            val updates = mutableMapOf<String, Any>(
                "switchStates" to states,
                "lastUpdated" to System.currentTimeMillis()
            )
            
            // If all switches are off, turn off the main device status
            if (states.none { it }) {
                updates["status"] = DeviceStatus.OFF.name
            } else if (state) {
                // If a switch is turned on, ensure the main device status is on
                updates["status"] = DeviceStatus.ON.name
            }

            firestore.collection("devices").document(deviceId).update(updates).await()
        }
    }

    suspend fun updateDeviceField(deviceId: String, field: String, value: Any) {
        firestore.collection("devices").document(deviceId)
            .update(field, value, "lastUpdated", System.currentTimeMillis()).await()
    }

    fun getDeviceById(deviceId: String): Flow<Device?> = callbackFlow {
        val listener = firestore.collection("devices").document(deviceId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val device = snapshot?.toObject(Device::class.java)?.copy(id = snapshot.id)
                trySend(device)
            }
        awaitClose { listener.remove() }
    }
}