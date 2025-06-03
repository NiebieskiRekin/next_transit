package com.example.nexttransit.model.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.nexttransit.model.calendar.CalendarInfo
import com.example.nexttransit.model.calendar.Event
import com.example.nexttransit.model.database.classes.DirectionsQuery
import com.example.nexttransit.model.database.classes.DirectionsQueryCrossRef
import com.example.nexttransit.model.routes.DirectionsResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface DirectionsDao {
    @Upsert
    suspend fun upsertCalendar(calendarInfo: CalendarInfo);

    @Query("SELECT * FROM calendarinfo")
    fun getAllCalendars(): Flow<List<CalendarInfo>>

    @Query("SELECT * FROM calendarinfo WHERE id = :id")
    suspend fun getCalendar(id: Long): CalendarInfo

    @Delete
    suspend fun deleteCalendar(calendarInfo: CalendarInfo)

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

    @Upsert
    suspend fun upsertDirectionsQueryCrossRef(directionsQueryCrossRef: DirectionsQueryCrossRef)

    @Upsert
    suspend fun upsertAllDirectionsQueryCrossRef(items: List<DirectionsQueryCrossRef>)

    @Query("SELECT * FROM directionsquerycrossref WHERE firstEventId = :e1 AND secondEventId = :e2 LIMIT 1")
    suspend fun getDirectionsQueryCrossRef(e1: Long, e2: Long): DirectionsQueryCrossRef

    @Query("SELECT * FROM directionsquerycrossref")
    fun getAllDirectionsQueryCrossRef(): Flow<List<DirectionsQueryCrossRef>>

    @Query("DELETE FROM directionsquerycrossref WHERE firstEventId = :e1 AND secondEventId = :e2")
    suspend fun deleteDirectionsQueryCrossRef(e1: Long, e2: Long)

    @Transaction
    @Query("""
    SELECT 
        d.directionsResponse,
        d.departAtOrArriveBy,
        e1.id AS first_event_id,
        e1.name AS first_event_name,
        e1.place AS first_event_place,
        e1.startDateTime AS first_event_startDateTime,
        e1.endDateTime AS first_event_endDateTime,
        e1.color as first_event_color,
        e1.eventId as first_event_eventId,
        e1.calendarId as first_event_calendarId,
        e2.id AS second_event_id,
        e2.name AS second_event_name,
        e2.place AS second_event_place,
        e2.startDateTime AS second_event_startDateTime,
        e2.endDateTime AS second_event_endDateTime,
        e2.color as second_event_color,
        e2.eventId as second_event_eventId,
        e2.calendarId as second_event_calendarId
    FROM directionsquerycrossref d 
    JOIN event e1 ON d.firstEventId = e1.id
    JOIN event e2 ON d.secondEventId = e2.id
    WHERE firstEventId = :e1 AND secondEventId = :e2 LIMIT 1
    """)
    suspend fun getDirectionsQuery(e1: Long, e2: Long): DirectionsQuery?


//    @Query("SELECT * FROM directionsquerycrossref JOIN event e1 ON directionsquerycrossref.firstEventId = e1.id JOIN event e2 ON directionsquerycrossref.secondEventId = e2.id ORDER BY e1.endDateTime ASC, e2.startDateTime ASC")
//    fun getAllDirectionsQueries(): Flow<List<DirectionsQuery>>
//
//
//    // Get all queries sorted by their first event's start time
//    @Query("SELECT * FROM directionsquerycrossref JOIN event e1 ON directionsquerycrossref.firstEventId = e1.id JOIN event e2 ON directionsquerycrossref.secondEventId = e2.id WHERE (e1.startDateTime >= :startTime AND e1.endDateTime <= :endTime)  ORDER BY e1.startDateTime ASC ")
//    fun getDirectionsQuerySortedByTime(startTime: Instant, endTime: Instant): Flow<List<DirectionsQuery>>
//
//    // Get next directions query
//    @Query("SELECT * FROM directionsquerycrossref JOIN event e1 ON directionsquerycrossref.firstEventId = e1.id JOIN event e2 ON directionsquerycrossref.secondEventId = e2.id WHERE (:currentTime <= e1.endDateTime+:delta AND :currentTime >= e1.startDateTime-:delta) ORDER BY e1.startDateTime ASC LIMIT 1")
//    suspend fun getNextDirectionsQueryByTime(currentTime: Instant, delta: Int = 3600): DirectionsQuery?
//
//    @Query("UPDATE directionsquerycrossref SET directionsResponse = :directionsResponse WHERE firstEventId = :firstEvent AND secondEventId = :secondEvent")
//    suspend fun updateDirectionsResponseForQuery(firstEvent: Long, secondEvent: Long, directionsResponse: DirectionsResponse)
//
//
//    @Transaction
//    suspend fun upsertDirectionsQuery(directionsQuery: DirectionsQuery){
//        upsertEvent(directionsQuery.firstEvent)
//        upsertEvent(directionsQuery.secondEvent)
//        upsertDirectionsQueryCrossRef(DirectionsQueryCrossRef(directionsQuery.firstEvent.id, directionsQuery.secondEvent.id, directionsQuery.departAtOrArriveBy, directionsQuery.directionsResponse))
//    }
//
//    @Transaction
//    suspend fun upsertAllDirectionsQuery(items: List<DirectionsQuery>){
//        val events = items.map { listOf(it.firstEvent, it.secondEvent) }.flatten()
//        upsertAllEvents(events)
//        upsertAllDirectionsQueryCrossRef(items.map { DirectionsQueryCrossRef(it.firstEvent.id, it.secondEvent.id, it.departAtOrArriveBy, it.directionsResponse) })
//    }
//
//    @Transaction
//    suspend fun deleteDirectionsQuery(directionsQuery: DirectionsQuery){
//        deleteDirectionsQueryCrossRef(directionsQuery.firstEvent.id, directionsQuery.secondEvent.id)
//    }
}