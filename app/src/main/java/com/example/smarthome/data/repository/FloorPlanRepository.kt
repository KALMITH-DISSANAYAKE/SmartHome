package com.example.smarthome.data.repository

import com.example.smarthome.data.model.FloorPlan
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FloorPlanRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getFloorPlans(): Flow<List<FloorPlan>> = callbackFlow {
        val listener = firestore.collection("floorPlans")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val plans = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FloorPlan::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(plans)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addFloorPlan(floorPlan: FloorPlan) {
        firestore.collection("floorPlans").add(floorPlan).await()
    }

    suspend fun deleteFloorPlan(id: String) {
        firestore.collection("floorPlans").document(id).delete().await()
    }

    suspend fun deleteAllFloorPlans() {
        val snapshot = firestore.collection("floorPlans").get().await()
        val batch = firestore.batch()
        snapshot.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }
}