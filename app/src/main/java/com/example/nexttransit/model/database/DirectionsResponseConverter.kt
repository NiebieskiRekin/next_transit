package com.example.nexttransit.model.database

import androidx.room.TypeConverter
import com.example.nexttransit.model.routes.DirectionsResponse
import kotlinx.serialization.json.Json

class DirectionsResponseConverter {
    @TypeConverter
    fun fromDirectionsResponse(directionsResponse: DirectionsResponse): String {
        return Json.encodeToString(directionsResponse)
    }

    @TypeConverter
    fun toDirectionsResponse(string: String): DirectionsResponse {
        return Json.decodeFromString(string)
    }
}