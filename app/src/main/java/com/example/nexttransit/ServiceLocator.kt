package com.example.nexttransit

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStore
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import com.example.nexttransit.ServiceLocator.apiService
import com.example.nexttransit.ServiceLocator.database
import com.example.nexttransit.ServiceLocator.firestore
import com.example.nexttransit.api.ApiCaller
import com.example.nexttransit.model.database.DirectionsDatabase
import com.example.nexttransit.model.settings.AppSettings
import com.example.nexttransit.model.settings.AppSettingsSerializer
import com.google.android.gms.common.api.Api
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.jvm.java

object ServiceLocator {

    @Volatile
    private var database: DirectionsDatabase? = null

    @Volatile
    private var apiService: ApiCaller? = null

    @Volatile
    private var dataStore: DataStore<AppSettings>? = null

    @SuppressLint("StaticFieldLeak")
    @Volatile
    private var firestore: FirebaseFirestore? = null

    @Volatile
    internal lateinit var auth: FirebaseAuth

    fun provideDatabase(context: Context): DirectionsDatabase {
        return database ?: synchronized(this) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                DirectionsDatabase::class.java,
                "directions.db"
            ).fallbackToDestructiveMigration(true).build().also { database = it }
        }
    }

    fun provideApiService(): ApiCaller {
        return apiService ?: synchronized(this) {
            apiService ?: ApiCaller.also { apiService = it }
        }
    }

    fun provideDataStore(context: Context): DataStore<AppSettings> {
        return dataStore ?: synchronized(this) {
            dataStore ?: DataStoreFactory.create(
                serializer = AppSettingsSerializer,
                produceFile = { context.applicationContext.filesDir.resolve("app-settings.json") }
            ).also { dataStore = it }
        }
    }

    fun provideFirestore(): FirebaseFirestore {
        return firestore ?: synchronized(this) {
            firestore ?: FirebaseFirestore.getInstance().also { firestore = it }
        }
    }

    fun provideAuth(): FirebaseAuth {
        return Firebase.auth
    }
}
