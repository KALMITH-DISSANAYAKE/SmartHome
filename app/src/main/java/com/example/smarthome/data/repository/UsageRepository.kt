package com.example.smarthome.data.repository

import com.example.smarthome.data.model.UsageLog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun logUsage(log: UsageLog) {
        firestore.collection("usageLogs").add(log).await()
    }

    fun getUsageLogs(): Flow<List<UsageLog>> = callbackFlow {
        val listener = firestore.collection("usageLogs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val logs = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(UsageLog::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(logs)
            }
        awaitClose { listener.remove() }
    }
}