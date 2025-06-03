package com.example.nexttransit.model.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.nexttransit.model.calendar.Event
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Upsert
    suspend fun upsertEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("SELECT * FROM event WHERE id = :id LIMIT 1")
    suspend fun queryEvent(id: Long): Event

    @Query("SELECT * FROM event ORDER BY startDateTime ASC")
    fun queryEvents(): Flow<List<Event>>

    @Upsert
    suspend fun upsertAllEvents(items: List<Event>)
}