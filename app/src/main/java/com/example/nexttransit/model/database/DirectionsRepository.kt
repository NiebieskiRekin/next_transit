package com.example.nexttransit.model.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.nexttransit.model.database.classes.DirectionsQuery
import com.example.nexttransit.model.database.classes.DirectionsQueryCrossRef
import com.example.nexttransit.model.database.dao.DirectionsQueryCrossRefDao
import com.example.nexttransit.model.database.dao.EventDao
import kotlin.collections.map

class DirectionsRepository(
    val eventDao: EventDao,
    val directionsQueryCrossRefDao: DirectionsQueryCrossRefDao
) {

//    @Transaction
    suspend fun upsertDirectionsQuery(directionsQuery: DirectionsQuery){
        eventDao.upsertEvent(directionsQuery.firstEvent)
        eventDao.upsertEvent(directionsQuery.secondEvent)
        directionsQueryCrossRefDao.upsertDirectionsQueryCrossRef(DirectionsQueryCrossRef(directionsQuery.firstEvent.id, directionsQuery.secondEvent.id, directionsQuery.departAtOrArriveBy, directionsQuery.directionsResponse))
    }

//    @Transaction
    suspend fun upsertAllDirectionsQuery(items: List<DirectionsQuery>){
        val events = items.map { listOf(it.firstEvent, it.secondEvent) }.flatten()
        eventDao.upsertAllEvents(events)
        directionsQueryCrossRefDao.upsertAllDirectionsQueryCrossRef(items.map { DirectionsQueryCrossRef(it.firstEvent.id, it.secondEvent.id, it.departAtOrArriveBy, it.directionsResponse) })
    }

//    @Transaction
    suspend fun deleteDirectionsQuery(directionsQuery: DirectionsQuery){
        directionsQueryCrossRefDao.deleteDirectionsQueryCrossRef(directionsQuery.firstEvent.id, directionsQuery.secondEvent.id)
    }
}
