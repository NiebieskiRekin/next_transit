package com.example.nexttransit.api

import android.content.Context
import android.util.Log
import androidx.credentials.exceptions.domerrors.NetworkError
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.example.nexttransit.model.database.classes.DirectionsQuery
import com.example.nexttransit.model.database.dao.DirectionsDao
import com.example.nexttransit.model.routes.DirectionsResponse
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

class NextTransitWorker(
    private val apiCaller: ApiCaller,
    private val directionsDao: DirectionsDao,
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "NextTransitWorker"
        private const val TAG = "NextTransitWorker"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Worker started. Checking for next upcoming transit.")
        var nextQuery: DirectionsQuery? = null
        try {
            nextQuery = directionsDao.getNextDirectionsQueryByTime(Clock.System.now())
            if (nextQuery == null) {
                Log.d(TAG, "No upcoming transit found needing an update.")
                return Result.success()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching from local database: ${e.message}", e)
            return Result.failure()
        }
        Log.d(
            TAG,
            "Calling API for next transit: ${nextQuery.firstEvent.place} -> ${nextQuery.secondEvent.place}, type: ${nextQuery.departAtOrArriveBy}"
        )
        var response: DirectionsResponse
        try {
            response = apiCaller.getDirectionsForEvents(
                nextQuery.firstEvent,
                nextQuery.secondEvent,
                nextQuery.departAtOrArriveBy
            )
            if (response.status == "OK") {
                directionsDao.updateDirectionsResponseForQuery(
                    nextQuery.firstEvent.id,
                    nextQuery.secondEvent.id,
                    response
                )
                Log.d(
                    TAG,
                    "Successfully updated directions for next transit: ${nextQuery.firstEvent.place} -> ${nextQuery.secondEvent.place}."
                )
                return Result.success(
                    Data.Builder().putString("directions", Json.encodeToString(response)).build()
                )
            } else {
                Log.w(
                    TAG,
                    "Failed to get directions from API for next transit: ${nextQuery.firstEvent.place} -> ${nextQuery.secondEvent.place}. Status: ${response.status}"
                )
                return Result.failure()
            }
        } catch (e: Exception) {
            if (e is NetworkError || e is java.net.UnknownHostException) {
                Log.e(TAG, "Network error calling API: ${e.message}, Retrying...", e)
                return Result.retry()
            }
            Log.e(TAG, "Error calling API: ${e.message}", e)
            return Result.failure()
        }
    }
}