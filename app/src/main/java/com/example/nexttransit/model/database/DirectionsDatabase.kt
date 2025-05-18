package com.example.nexttransit.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.nexttransit.model.calendar.Event

@Database(
    entities = [DirectionsQuery::class, Event::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class DirectionsDatabase: RoomDatabase() {
    abstract val directionsQueryDao: DirectionsQueryDao
    abstract val eventDao: EventDao
    abstract val directionsQueryFullDao: DirectionsQueryFullDao
}