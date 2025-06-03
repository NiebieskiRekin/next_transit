package com.example.nexttransit.api

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class FirestoreSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    companion object {
        const val WORK_NAME = "FirestoreSyncWorker"
        private const val TAG = "FirestoreSyncWorker"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Worker started. Checking for Syncing to Firestore.")
        return Result.success()
    }
}