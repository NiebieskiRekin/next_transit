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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Database(
    entities = [Event::class, DirectionsQueryCrossRef::class, CalendarInfo::class],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class DirectionsDatabase : RoomDatabase() {
    abstract val directionsQueryDao: DirectionsDao
}

@Module
@InstallIn(SingletonComponent::class)
object DirectionsDatabaseModule {
    @Provides
    fun provideDirectionsQueryDao(database: DirectionsDatabase): DirectionsDao {
        return database.directionsQueryDao
    }

    @Provides
    @Singleton
    fun provideDirectionsDatabase(@ApplicationContext context: Context): DirectionsDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            DirectionsDatabase::class.java,
            "directions.db"
        ).fallbackToDestructiveMigration(true).build()
    }
}