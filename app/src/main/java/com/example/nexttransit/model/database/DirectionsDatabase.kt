package com.example.nexttransit.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.nexttransit.model.calendar.CalendarInfo
import com.example.nexttransit.model.calendar.Event
import com.example.nexttransit.model.database.classes.DirectionsQueryCrossRef
import com.example.nexttransit.model.database.dao.DirectionsDao

@Database(
    entities = [Event::class, DirectionsQueryCrossRef::class, CalendarInfo::class],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class DirectionsDatabase : RoomDatabase() {
    abstract val directionsQueryDao: DirectionsDao
}

object DirectionsDatabaseModule {
    fun provideDirectionsQueryDao(database: DirectionsDatabase): DirectionsDao {
        return database.directionsQueryDao
    }
}