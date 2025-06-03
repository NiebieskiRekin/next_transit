package com.example.nexttransit.model.database.classes

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.nexttransit.model.calendar.Event
import com.example.nexttransit.model.routes.DirectionsResponse
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    primaryKeys = ["firstEventId", "secondEventId"],
    foreignKeys = [
        ForeignKey(
            entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["firstEventId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["secondEventId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("firstEventId", "secondEventId"), Index("firstEventId"), Index("secondEventId")]
)
data class DirectionsQueryCrossRef (
    val firstEventId: Long,
    val secondEventId: Long,
    val departAtOrArriveBy: DepartAtOrArriveBy,
    val directionsResponse: DirectionsResponse
) {
    constructor() :this(0, 0, DepartAtOrArriveBy.DepartAt, DirectionsResponse())
}