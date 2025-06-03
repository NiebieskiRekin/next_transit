package com.example.nexttransit.model.database.classes

import androidx.room.Embedded
import androidx.room.Relation
import com.example.nexttransit.model.calendar.Event
import com.example.nexttransit.model.routes.DirectionsResponse
import kotlinx.serialization.Serializable

@Serializable
data class DirectionsQuery(
    @Embedded(prefix = "first_event_")
    val firstEvent: Event,
    @Embedded(prefix = "second_event_")
    val secondEvent: Event,
    val departAtOrArriveBy: DepartAtOrArriveBy,
    val directionsResponse: DirectionsResponse,
)