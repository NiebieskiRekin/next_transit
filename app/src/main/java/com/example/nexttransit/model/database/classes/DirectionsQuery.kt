package com.example.nexttransit.model.database.classes

import androidx.room.Embedded
import androidx.room.Relation
import com.example.nexttransit.model.calendar.Event
import com.example.nexttransit.model.routes.DirectionsResponse
import kotlinx.serialization.Serializable

@Serializable
data class DirectionsQuery(
    //    @Relation(
//        parentColumn = "firstEvent",
//        entityColumn = "id"
//    )
    val firstEvent: Event,

//    @Relation(
//        parentColumn = "secondEvent",
//        entityColumn = "id"
//    )
    val secondEvent: Event,

    val departAtOrArriveBy: DepartAtOrArriveBy,
//    @Relation(
//        parentColumn = "directionsResponse",
//        entityColumn = "uuid"
//    )
    val directionsResponse: DirectionsResponse,
)