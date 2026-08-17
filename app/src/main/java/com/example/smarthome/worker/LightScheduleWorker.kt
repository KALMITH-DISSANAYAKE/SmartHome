package com.example.smarthome.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class LightScheduleWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return Result.success()
    }
}
