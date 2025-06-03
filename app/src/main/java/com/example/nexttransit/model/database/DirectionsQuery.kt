package com.example.nexttransit.model.database

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
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["secondEvent"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("firstEvent", "secondEvent"),Index("firstEvent"),Index("secondEvent")]
)
data class DirectionsQuery (
    val firstEvent: Long,
    val secondEvent: Long,
    val directionsResponse: DirectionsResponse,
    val departAtOrArriveBy: DepartAtOrArriveBy
)