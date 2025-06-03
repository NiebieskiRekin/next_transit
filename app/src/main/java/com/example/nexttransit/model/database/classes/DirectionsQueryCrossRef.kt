package com.example.nexttransit.model.database.classes

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.nexttransit.model.calendar.Event
import com.example.nexttransit.model.routes.DirectionsResponse
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    primaryKeys = ["firstEvent", "secondEvent"],
    foreignKeys = [
        ForeignKey(
            entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["firstEvent"],
            onDelete = ForeignKey.Companion.CASCADE
        ),
        ForeignKey(
            entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["secondEvent"],
            onDelete = ForeignKey.Companion.CASCADE
        )
    ],
    indices = [Index("firstEvent", "secondEvent"), Index("firstEvent"), Index("secondEvent")]
)
data class DirectionsQueryCrossRef (
    val firstEvent: Long,
    val secondEvent: Long,
    val departAtOrArriveBy: DepartAtOrArriveBy,
    val directionsResponse: DirectionsResponse
) {
    constructor() :this(0, 0, DepartAtOrArriveBy.DepartAt, DirectionsResponse())
}