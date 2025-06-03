package com.example.nexttransit.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.nexttransit.model.calendar.CalendarInfo
import com.example.nexttransit.model.calendar.Event
import com.example.nexttransit.model.database.classes.DirectionsQueryCrossRef
import com.example.nexttransit.model.database.dao.CalendarInfoDao
import com.example.nexttransit.model.database.dao.EventDao

@Database(
    entities = [DirectionsQueryCrossRef::class, Event::class, CalendarInfo::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class DirectionsDatabase: RoomDatabase() {
    abstract val eventDao: EventDao;
    abstract val calendarInfoDao: CalendarInfoDao;
    abstract val directionsQueryCrossRefDao: DirectionsQueryCrossRef;

    companion object {
        @Volatile
        private var INSTANCE: DirectionsDatabase? = null

        fun getDatabase(context: Context): DirectionsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DirectionsDatabase::class.java,
                    "directions.db"
                ).build()
                INSTANCE = instance
                instance
            }

        }
    }
}