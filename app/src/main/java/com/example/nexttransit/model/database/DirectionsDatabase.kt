package com.example.nexttransit.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [DirectionsQuery::class],
    version = 1
)
@TypeConverters(DirectionsResponseConverter::class,EventConverter::class)
abstract class DirectionsDatabase: RoomDatabase() {
    abstract val directionsQueryDao: DirectionsQueryDao
}