package com.example.nexttransit.model.calendar

import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

val TZ = TimeZone.currentSystemDefault()

/**
 * Represents a slot in the daily schedule, which can either be an event or a gap.
 */
sealed interface ScheduleSlotItem {
    val startTime: LocalTime
    val endTime: LocalTime

    data class EventItem(val event: Event) : ScheduleSlotItem {
        override val startTime: LocalTime get() = event.startDateTime.toLocalDateTime(TZ).time
        override val endTime: LocalTime get() = event.endDateTime.toLocalDateTime(TZ).time
    }

    data class GapItem(
        override val startTime: LocalTime,
        override val endTime: LocalTime
    ) : ScheduleSlotItem
}