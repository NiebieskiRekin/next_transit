package com.example.nexttransit.model.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.nexttransit.model.calendar.CalendarInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarInfoDao {
    @Upsert
    suspend fun upsertCalendar(calendarInfo: CalendarInfo);

    @Query("SELECT * FROM calendarinfo")
    fun getAllCalendars(): Flow<List<CalendarInfo>>

    @Query("SELECT * FROM calendarinfo WHERE id = :id")
    suspend fun getCalendar(id: Long): CalendarInfo

    @Delete
    suspend fun deleteCalendar(calendarInfo: CalendarInfo)
}