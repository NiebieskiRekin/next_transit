package com.example.nexttransit.model.calendar

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import java.time.LocalTime
import java.util.UUID

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
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val place: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val color: Color? = null,
    val eventId: Long
)