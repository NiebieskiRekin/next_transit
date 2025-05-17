package com.example.nexttransit.model.calendar

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.google.android.gms.common.internal.Objects.hashCode
import java.time.LocalDateTime

/**
 * Represents an event in the schedule.
 *
 * @param id Unique identifier for the event.
 * @param name Name or title of the event.
 * @param place Location of the event.
 * @param startTime Start time of the event.
 * @param endTime End time of the event.
 * @param color Optional color to use for displaying this event.
 */
@Immutable
data class Event(
    val id: Int,
    val name: String,
    val place: String,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val color: Color? = null,
    val eventId: Long
){
    constructor(name: String, place: String, startDateTime: LocalDateTime, endDateTime: LocalDateTime, eventId: Long) : this(
        hashCode(name, place, startDateTime, endDateTime),
        name,
        place,
        startDateTime,
        endDateTime,
        null,
        eventId
    )
    constructor(name: String, place: String, startDateTime: LocalDateTime, endDateTime: LocalDateTime, color: Color?, eventId: Long) : this(
        hashCode(name, place, startDateTime, endDateTime),
        name,
        place,
        startDateTime,
        endDateTime,
        color,
        eventId
    )
}