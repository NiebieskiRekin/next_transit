package com.example.nexttransit.model.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DirectionsQuery::class],
    version = 1
)
abstract class DirectionsDatabase: RoomDatabase() {
    abstract val directionsQueryDao: DirectionsQueryDao
}