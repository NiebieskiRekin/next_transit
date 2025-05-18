package com.example.nexttransit.model.calendar

import android.content.ContentResolver
import android.provider.CalendarContract
import androidx.compose.ui.graphics.Color
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

fun getAvailableCalendars(contentResolver: ContentResolver): List<CalendarInfo> {
    val calendarsList = mutableListOf<CalendarInfo>()
    val projection = arrayOf(
        CalendarContract.Calendars._ID,
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
        CalendarContract.Calendars.ACCOUNT_NAME,
        CalendarContract.Calendars.OWNER_ACCOUNT,
        CalendarContract.Calendars.CALENDAR_COLOR
    )

    // You might want to filter for specific account types or visible calendars only
    // val selection = "${CalendarContract.Calendars.VISIBLE} = ?"
    // val selectionArgs = arrayOf("1")

    val cursor = contentResolver.query(
        CalendarContract.Calendars.CONTENT_URI,
        projection,
        null, // No selection (gets all calendars) or use selection above
        null, // No selection arguments
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + " ASC" // Sort order
    )

    cursor?.use {
        while (it.moveToNext()) {
            val idIndex = it.getColumnIndex(CalendarContract.Calendars._ID)
            val displayNameIndex = it.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
            val accountNameIndex = it.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME)
            val ownerNameIndex = it.getColumnIndex(CalendarContract.Calendars.OWNER_ACCOUNT)
            val colorIndex = it.getColumnIndex(CalendarContract.Calendars.CALENDAR_COLOR)


            val id = if (idIndex != -1) it.getLong(idIndex) else -1L
            val displayName = if (displayNameIndex != -1) it.getString(displayNameIndex) else "N/A"
            val accountName = if (accountNameIndex != -1) it.getString(accountNameIndex) else "N/A"
            val ownerName = if (ownerNameIndex != -1) it.getString(ownerNameIndex) else "N/A"
            val color = if (colorIndex != -1 && !it.isNull(colorIndex)) it.getInt(colorIndex) else null


            if (id != -1L) {
                calendarsList.add(CalendarInfo(id, displayName, accountName, ownerName,
                    if (color != null) Color(color) else null
                ))
            }
        }
    }
    return calendarsList
}


fun getEvents(
    contentResolver: ContentResolver,
    calendarId: Long,
    startDateMillis: Long,
    endDateMillis: Long
): List<Event> {
    val eventsList = mutableListOf<Event>()

    val projection = arrayOf(
        CalendarContract.Events.TITLE,
        CalendarContract.Events.EVENT_LOCATION,
        CalendarContract.Events.DTSTART,
        CalendarContract.Events.DTEND,
        CalendarContract.Events.EVENT_COLOR, // Optional: if you want to use event-specific colors
        // Add other fields you need, e.g., CalendarContract.Events.DESCRIPTION
        CalendarContract.Events._ID
    )

    // Selection: query for events within the date range and for the specific calendar
    val selection = "(${CalendarContract.Events.CALENDAR_ID} = ?) AND " +
            "(${CalendarContract.Events.DTSTART} >= ?) AND " +
            "(${CalendarContract.Events.DTEND} <= ? OR (${CalendarContract.Events.DTEND} IS NULL AND ${CalendarContract.Events.DURATION} IS NOT NULL))"
    // Note on DTEND: For recurring events without an explicit end time but with a duration,
    // DTEND might be null. The query above is a simplified example.
    // Handling all-day events and recurring events properly can be complex.

    val selectionArgs = arrayOf(
        calendarId.toString(),
        startDateMillis.toString(),
        endDateMillis.toString()
    )

    val cursor = contentResolver.query(
        CalendarContract.Events.CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        CalendarContract.Events.DTSTART + " ASC" // Sort by start time
    )

    cursor?.use {
        val titleIndex = it.getColumnIndex(CalendarContract.Events.TITLE)
        val locationIndex = it.getColumnIndex(CalendarContract.Events.EVENT_LOCATION)
        val dtStartIndex = it.getColumnIndex(CalendarContract.Events.DTSTART)
        val dtEndIndex = it.getColumnIndex(CalendarContract.Events.DTEND)
        val eventColorIndex = it.getColumnIndex(CalendarContract.Events.EVENT_COLOR)


        while (it.moveToNext()) {
            val title = if (titleIndex != -1) it.getString(titleIndex) else "No Title"
            val location = if (locationIndex != -1) it.getString(locationIndex) ?: "" else ""
            val startTimeMillis = if (dtStartIndex != -1) it.getLong(dtStartIndex) else 0L
            // DTEND can be null for events with a DURATION instead.
            // For simplicity, this example assumes DTEND is usually present.
            // A more robust solution would check DURATION if DTEND is null.
            val endTimeMillis = if (dtEndIndex != -1 && !it.isNull(dtEndIndex)) it.getLong(dtEndIndex) else startTimeMillis // Fallback

            val eventColorInt = if (eventColorIndex != -1 && !it.isNull(eventColorIndex)) it.getInt(eventColorIndex) else null
            val eventColor = eventColorInt?.let { colorVal -> Color(colorVal) }
            val eventId = it.getColumnIndex(CalendarContract.Events._ID).toLong()

            // Convert Millis to LocalTime for your Event data class
            val startTime = Instant.fromEpochMilliseconds(startTimeMillis)
            val endTime =Instant.fromEpochMilliseconds(endTimeMillis)

            // Ensure endTime is after startTime if it was a fallback
            val correctedEndTime = if (endTime < startTime || endTime == startTime) {
                startTime.plus(1, DateTimeUnit.HOUR) // Default to 1 hour duration if end time is invalid
            } else {
                endTime
            }


            if (startTimeMillis > 0) { // Basic validation
                eventsList.add(
                    Event( // Use your Event class
                        name = title,
                        place = location,
                        startDateTime = startTime,
                        endDateTime = correctedEndTime,
                        color = eventColor,
                        eventId = eventId
                    )
                )
            }
        }
    }
    return eventsList
}


fun Instant.getLocalTime(timeZone: TimeZone): LocalTime {
    return this.toLocalDateTime(timeZone).time
}

/**
 * Generates a list of [ScheduleSlotItem]s for a given day,
 * filling gaps between events.
 *
 * @param events A list of [Event]s for the day, assumed to be non-overlapping
 * and ideally pre-sorted by start time.
 * @return A list of [ScheduleSlotItem]s representing the chronological schedule.
 */
fun generateScheduleSlots(
    events: List<Event>,
    dayStart: LocalTime,
    dayEnd: LocalTime
): List<ScheduleSlotItem> {
    val scheduleItems = mutableListOf<ScheduleSlotItem>()
    var currentTime = dayStart

    // Sort events by start time to ensure correct chronological processing
    val sortedEvents = events.sortedBy { it.startDateTime }

    for (event in sortedEvents) {
        // If there's a gap before this event starts
        val eventStartTime = event.startDateTime.getLocalTime(TZ)
        if (eventStartTime > currentTime) {
            scheduleItems.add(ScheduleSlotItem.GapItem(currentTime, eventStartTime))
        }
        // Add the event itself
        scheduleItems.add(ScheduleSlotItem.EventItem(event))
        // Move current time to the end of this event
        currentTime = event.endDateTime.getLocalTime(TZ)
    }

    // If there's a gap after the last event until the end of the day
    if (currentTime < dayEnd) {
        // Ensure we don't add a zero-duration gap if currentTime is exactly dayEnd
        // (though LocalTime.MAX makes this unlikely to be equal unless explicitly set)
        if (currentTime < dayEnd) {
            scheduleItems.add(ScheduleSlotItem.GapItem(currentTime, dayEnd))
        }
    }
    return scheduleItems
}