package com.example.nexttransit.model.calendar

import java.time.LocalTime

/**
 * Represents a slot in the daily schedule, which can either be an event or a gap.
 */
sealed interface ScheduleSlotItem {
    val startTime: LocalTime
    val endTime: LocalTime

    data class EventItem(val event: Event) : ScheduleSlotItem {
        override val startTime: LocalTime get() = event.startDateTime.toLocalTime()
        override val endTime: LocalTime get() = event.endDateTime.toLocalTime()
    }

    data class GapItem(
        override val startTime: LocalTime,
        override val endTime: LocalTime
    ) : ScheduleSlotItem
}