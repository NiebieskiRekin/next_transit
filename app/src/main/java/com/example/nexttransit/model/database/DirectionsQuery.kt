package com.example.nexttransit.model.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.nexttransit.model.calendar.Event
import com.example.nexttransit.model.routes.DirectionsResponse
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class DirectionsQuery (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val firstEvent: Event,
    val secondEvent: Event,
    val directionsResponse: DirectionsResponse
)