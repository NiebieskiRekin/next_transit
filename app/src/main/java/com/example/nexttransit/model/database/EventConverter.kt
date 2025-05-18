package com.example.nexttransit.model.database

import androidx.room.TypeConverter
import com.example.nexttransit.model.calendar.Event
import kotlinx.serialization.json.Json

class EventConverter {
    @TypeConverter
    fun fromEvent(input: Event): String {
        return Json.encodeToString(input)
    }

    @TypeConverter
    fun toEvent(input: String): Event {
        return Json.decodeFromString(input)
    }
}