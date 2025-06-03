package com.example.nexttransit.model.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.nexttransit.model.database.classes.DirectionsQuery
import com.example.nexttransit.model.database.classes.DirectionsQueryCrossRef
import com.example.nexttransit.model.routes.DirectionsResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface DirectionsQueryCrossRefDao {
    @Upsert
    suspend fun upsertDirectionsQueryCrossRef(directionsQueryCrossRef: DirectionsQueryCrossRef)

    @Upsert
    suspend fun upsertAllDirectionsQueryCrossRef(items: List<DirectionsQueryCrossRef>)

    @Query("SELECT * FROM directionsquerycrossref WHERE firstEvent = :e1 AND secondEvent = :e2 LIMIT 1")
    suspend fun getDirectionsQueryCrossRef(e1: Long, e2: Long): DirectionsQueryCrossRef

    @Query("SELECT * FROM directionsquerycrossref")
    fun getAllDirectionsQueryCrossRef(): Flow<List<DirectionsQueryCrossRef>>

    @Query("DELETE FROM directionsquerycrossref WHERE firstEvent = :e1 AND secondEvent = :e2")
    suspend fun deleteDirectionsQueryCrossRef(e1: Long, e2: Long)


    @Query("SELECT * FROM directionsquerycrossref WHERE firstEvent = :e1 AND secondEvent = :e2 LIMIT 1")
    suspend fun getDirectionsQuery(e1: Long, e2: Long): DirectionsQuery


    @Query("SELECT * FROM directionsquerycrossref JOIN event e1 ON directionsquerycrossref.firstEvent = e1.id JOIN event e2 ON directionsquerycrossref.secondEvent = e2.id ORDER BY e1.endDateTime ASC, e2.startDateTime ASC")
    fun getAllDirectionsQueries(): Flow<List<DirectionsQuery>>


    // Get all queries sorted by their first event's start time
    @Query("SELECT * FROM directionsquerycrossref JOIN event e1 ON directionsquerycrossref.firstEvent = e1.id JOIN event e2 ON directionsquerycrossref.secondEvent = e2.id WHERE (e1.startDateTime >= :startTime AND e1.endDateTime <= :endTime)  ORDER BY e1.startDateTime ASC ")
    fun getDirectionsQuerySortedByTime(startTime: Instant, endTime: Instant): Flow<List<DirectionsQuery>>

    // Get next directions query
    @Query("SELECT * FROM directionsquerycrossref JOIN event e1 ON directionsquerycrossref.firstEvent = e1.id JOIN event e2 ON directionsquerycrossref.secondEvent = e2.id WHERE (:currentTime <= e1.endDateTime+:delta AND :currentTime >= e1.startDateTime-:delta) ORDER BY e1.startDateTime ASC LIMIT 1")
    suspend fun getNextDirectionsQueryByTime(currentTime: Instant, delta: Int = 3600): DirectionsQuery?

    @Query("UPDATE directionsquerycrossref SET directionsResponse = :directionsResponse WHERE firstEvent = :firstEvent AND secondEvent = :secondEvent")
    suspend fun updateDirectionsResponseForQuery(firstEvent: Long, secondEvent: Long, directionsResponse: DirectionsResponse)
}