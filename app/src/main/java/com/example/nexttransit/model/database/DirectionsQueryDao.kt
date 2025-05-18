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

    @Query("SELECT * FROM directionsquery WHERE id = :id LIMIT 1")
    fun getDirectionsQuery(id: Int): DirectionsQuery

    @Query("SELECT * FROM directionsquery ORDER BY id ASC")
    fun getAllDirectionsQueries(): Flow<List<DirectionsQuery>>

}