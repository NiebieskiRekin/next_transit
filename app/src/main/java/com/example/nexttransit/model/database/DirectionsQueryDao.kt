package com.example.nexttransit.model.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DirectionsQueryDao {

    @Upsert
    suspend fun upsertDirectionsQuery(directionsQuery: DirectionsQuery)

    @Delete
    suspend fun deleteDirectionsQuery(directionsQuery: DirectionsQuery)

    @Query("SELECT * FROM directionsquery WHERE firstEvent = :e1 AND secondEvent = :e2 LIMIT 1")
    fun getDirectionsQuery(e1: Long, e2: Long): DirectionsQuery

    @Query("SELECT * FROM directionsquery JOIN event e1 ON directionsquery.firstEvent = e1.id JOIN event e2 ON directionsquery.secondEvent = e2.id ORDER BY e1.endDateTime ASC, e2.startDateTime ASC")
    fun getAllDirectionsQueries(): Flow<List<DirectionsQuery>>

}