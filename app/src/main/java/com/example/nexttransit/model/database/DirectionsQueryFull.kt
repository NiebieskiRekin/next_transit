package com.example.nexttransit.model.database

import androidx.room.Embedded
import androidx.room.Relation
import com.example.nexttransit.model.calendar.Event
import com.example.nexttransit.model.routes.DirectionsResponse
import kotlinx.serialization.Serializable

@Serializable
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
){
    constructor () : this(DirectionsQuery(-1,-1, DirectionsResponse()), Event(), Event())
}
