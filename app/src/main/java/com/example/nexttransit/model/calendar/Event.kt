package com.example.nexttransit.model.calendar

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.nexttransit.model.ColorAsIntSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.Objects.hash

/**
 * Represents an event in the schedule.
 *
 * @param id Unique identifier for the event.
 * @param name Name or title of the event.
 * @param place Location of the event.
 * @param startDateTime Start time of the event.
 * @param endDateTime End time of the event.
 * @param color Optional color to use for displaying this event.
 */
@Immutable
@Serializable
@Entity
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val place: String,
    val startDateTime: Instant,
    val endDateTime: Instant,
    @Serializable(with = ColorAsIntSerializer::class)
    val color: Color? = null,
    val eventId: Long
){
    constructor(name: String, place: String, startDateTime: Instant, endDateTime: Instant, color: Color?, eventId: Long) : this(
        hash(name, place, startDateTime, endDateTime),
        name,
        place,
        startDateTime,
        endDateTime,
        color,
        eventId
    )

    constructor(): this(
        0,
        "",
        "",
        Instant.fromEpochMilliseconds(0),
        Instant.fromEpochMilliseconds(0),
        null,
        0
    )
}