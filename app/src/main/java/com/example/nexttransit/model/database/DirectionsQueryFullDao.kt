package com.example.nexttransit.model.database

import androidx.room.Dao
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.nexttransit.model.calendar.Event

@Dao
interface DirectionsQueryFullDao {

    @Upsert
    suspend fun upsertEvent(event: Event)

    @Upsert
    suspend fun upsertDirectionsQuery(directionsQuery: DirectionsQuery)

    @Transaction
    suspend fun upsertDirectionsQueryFull(directionsQueryFull: DirectionsQueryFull) {
        upsertEvent(directionsQueryFull.firstEvent)
        upsertEvent(directionsQueryFull.secondEvent)
        upsertDirectionsQuery(directionsQueryFull.directionsQuery)
    }
}