package com.example.nexttransit.model.database

import androidx.room.Embedded
import androidx.room.Relation
import com.example.nexttransit.model.calendar.Event

data class DirectionsQueryFull(
    @Embedded val directionsQuery: DirectionsQuery,

    @Relation(
        parentColumn = "firstEvent",
        entityColumn = "id"
    )
    val firstEvent: Event,

    @Relation(
        parentColumn = "secondEvent",
        entityColumn = "id"
    )
    val secondEvent: Event,
)
