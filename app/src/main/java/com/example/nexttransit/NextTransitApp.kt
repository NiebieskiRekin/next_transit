package com.example.nexttransit

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.nexttransit.api.ApiCaller
import com.example.nexttransit.api.NextTransitWorker
import com.example.nexttransit.model.database.dao.DirectionsDao
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NextTransitApp() : Application(),
    Configuration.Provider {

    @Inject
    lateinit var workerFactory: NextTransitWorkerFactory

    override val workManagerConfiguration: Configuration = Configuration.Builder()
        .setMinimumLoggingLevel(Log.DEBUG).setWorkerFactory(workerFactory).build()
}


class NextTransitWorkerFactory @Inject constructor(
    private val apiCaller: ApiCaller,
    private val directionsDao: DirectionsDao
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? = NextTransitWorker(apiCaller, directionsDao, appContext, workerParameters)
}
