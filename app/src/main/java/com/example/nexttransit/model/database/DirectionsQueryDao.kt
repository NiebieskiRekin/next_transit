package com.example.nexttransit.model.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.nexttransit.model.calendar.Event
import com.example.nexttransit.model.routes.DirectionsResponse
import kotlinx.coroutines.flow.Flow

@Dao
interface DirectionsQueryDao {

    @Upsert
    suspend fun upsertDirectionsQuery(directionsQuery: DirectionsQuery)

    @Upsert
    suspend fun upsertAllDirectionsQuery(items: List<DirectionsQuery>)

    @Upsert
    suspend fun upsertAllEvents(items: List<Event>)

    @Transaction
    suspend fun upsertAllDirectionsQueryFull(items: List<DirectionsQueryFull>){
        val events = items.map { listOf(it.firstEvent, it.secondEvent) }.flatten()
        val directionsQueries = items.map { it.directionsQuery }
        upsertAllEvents(events)
        upsertAllDirectionsQuery(directionsQueries)
    }

    @Delete
    suspend fun deleteDirectionsQuery(directionsQuery: DirectionsQuery)

    @Query("SELECT * FROM directionsquery WHERE firstEvent = :e1 AND secondEvent = :e2 LIMIT 1")
    fun getDirectionsQuery(e1: Long, e2: Long): DirectionsQuery

    @Transaction
    @Query("SELECT * FROM directionsquery JOIN event e1 ON directionsquery.firstEvent = e1.id JOIN event e2 ON directionsquery.secondEvent = e2.id ORDER BY e1.endDateTime ASC, e2.startDateTime ASC")
    fun getAllDirectionsQueries(): Flow<List<DirectionsQueryFull>>

    @Upsert
    suspend fun upsertEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("SELECT * FROM event WHERE id = :id LIMIT 1")
    suspend fun queryEvent(id: Long): Event

    @Query("SELECT * FROM event ORDER BY startDateTime ASC")
    fun queryEvents(): Flow<List<Event>>

    @Transaction
    suspend fun upsertDirectionsQueryFull(directions: DirectionsResponse, firstEvent: Event, secondEvent: Event) {
        upsertEvent(firstEvent)
        upsertEvent(secondEvent)
        upsertDirectionsQuery(DirectionsQuery(directionsResponse = directions,firstEvent = firstEvent.id, secondEvent = secondEvent.id))
    }
}