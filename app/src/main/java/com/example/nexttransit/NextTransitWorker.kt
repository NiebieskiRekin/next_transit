package com.example.nexttransit

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nexttransit.api.ApiCaller
import com.example.nexttransit.model.database.DirectionsQueryDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@HiltWorker
class NextTransitWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val directionsQueryDao: DirectionsQueryDao,
    private val apiCaller: ApiCaller
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "UpcomingTransitWorker"
        private const val TAG = "UpcomingTransitWorker"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Worker started. Checking for next upcoming transit.")

        try {
            val now: Instant = Clock.System.now()

            // 1. Fetch all queries sorted by start time
            val nextQuery = directionsQueryDao.getNextDirectionsQueryByTime(now);

            if (nextQuery == null) {
                Log.d(TAG, "No upcoming transit found needing an update.")
                return Result.success() // No work to do
            }

            Log.d(TAG, "Found next transit to update: ID ${nextQuery.firstEvent.id}:${nextQuery.secondEvent.id}, Origin: ${nextQuery.firstEvent.place}, Dest: ${nextQuery.secondEvent.place}, DepartAt: ${nextQuery.directionsQuery.directionsResponse.routes.firstOrNull()?.legs?.firstOrNull()?.departureTime}")

            // 3. Call the Google Directions API
            val directionsResponse = apiCaller.getDirectionsByNameAndDepartAt(
                originName = nextQuery.originName,
                destinationName = nextQuery.destinationName,
                departAtMillis = nextQuery.firstEventStartTimeMillis
            )

            // 4. Update the local database if successful
            if (directionsResponse != null) {
                // Serialize the response to JSON (or handle based on your model)
                // This is highly dependent on your DirectionsResponse structure
                // and how you want to store it.
                val responseJson = try {
                    gson.toJson(directionsResponse)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to serialize DirectionsResponse to JSON", e)
                    null // Or handle error appropriately
                }

                if (responseJson != null) {
                    directionsQueryDao.updateDirectionsResponseForQuery(
                        queryId = nextQuery.id,
                        newResponseJson = responseJson,
                        refreshTime = nowMillis
                    )
                    Log.d(TAG, "Successfully updated directions for query ID ${nextQuery.id} in Room.")
                } else {
                    Log.w(TAG, "Directions API call successful, but response serialization failed for query ID ${nextQuery.id}.")
                    // Decide if this is a retryable error or not
                }
            } else {
                Log.w(TAG, "Failed to get directions from API for query ID ${nextQuery.id}.")
                // Consider if this is a retryable error.
                // If the API call fails consistently for a valid query, you might not want to retry indefinitely.
                return Result.retry() // Or Result.failure() if it's a non-transient error
            }

            return Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "Error in UpcomingTransitWorker: ${e.message}", e)
            return Result.retry() // Retry for most exceptions
        }
    }
}