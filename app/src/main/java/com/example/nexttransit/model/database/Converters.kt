package com.example.nexttransit.model.database

import androidx.room.TypeConverter
import com.example.nexttransit.model.routes.DirectionsResponse
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromULong(value: ULong?): Long? {
        return value?.toLong()
    }

    @TypeConverter
    fun toULong(value: Long?): ULong? {
        return value?.toULong()
    }
    
    @TypeConverter
    fun fromInstant(value: Instant): Long {
        return value.toEpochMilliseconds()
    }

    @TypeConverter
    fun toInstant(value: Long): Instant {
        return Instant.fromEpochMilliseconds(value)
    }

    @TypeConverter
    fun fromDirectionsResponse(value: DirectionsResponse): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toDirectionsResponse(value: String): DirectionsResponse {
        return Json.decodeFromString(value)
    }
}