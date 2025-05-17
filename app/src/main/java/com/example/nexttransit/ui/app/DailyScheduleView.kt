package com.example.nexttransit.ui.app

import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.example.nexttransit.model.calendar.Event
import com.example.nexttransit.model.calendar.ScheduleSlotItem
import com.example.nexttransit.model.calendar.generateScheduleSlots
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.collections.sortedBy

/**
 * Main composable for displaying the daily schedule.
 *
 * @param date The date for which the schedule is being displayed (for context or future header).
 * @param events List of [Event]s for the given date.
 * @param modifier Modifier for this composable.
 * @param dayStart The time considered as the start of the day for scheduling.
 * @param dayEnd The time considered as the end of the day for scheduling.
 */
@Composable
fun DailyScheduleView(
    date: LocalDate,
    events: List<Event>,
    modifier: Modifier = Modifier,
    dayStart: LocalTime = LocalTime.MIN,
    dayEnd: LocalTime = LocalTime.MAX
) {
    val scheduleSlots = remember(events, dayStart, dayEnd) {
        generateScheduleSlots(events, dayStart, dayEnd)
    }

    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    if (scheduleSlots.isEmpty()) {
        // Handle empty day - could show a full day gap or a message
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GapCard(startTime = dayStart, endTime = dayEnd, timeFormatter = timeFormatter)
        }
        return
    }

    var firstEvent by remember { mutableStateOf<Event?>(null)}
    var secondEvent by remember { mutableStateOf<Event?>(null)}
    val context = LocalContext.current


    fun onEventClick(event: Event) {
        // Edit event in system calendar
        val uri: Uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.eventId)
        val intent = Intent(Intent.ACTION_EDIT)
            .setData(uri).putExtra(CalendarContract.Events.TITLE, event.name)
        startActivity(context,intent,null)
    }

    fun onLongPressEvent(event: Event) {
        Log.d("CalendarAccess", firstEvent.toString() + "\n" + secondEvent.toString())
        if (firstEvent?.id == event.id){
            firstEvent = null
            return
        }

        if (secondEvent?.id == event.id){
            secondEvent = null
            return
        }

        if (firstEvent == null){
            firstEvent = event
        } else if (secondEvent == null) {
            secondEvent = event
        }
    }


    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp), // Space between cards
        contentPadding = PaddingValues(0.dp,8.dp)
    ) {
        items(scheduleSlots) { slotItem ->
            when (slotItem) {
                is ScheduleSlotItem.EventItem -> {
                    EventCard(
                        event = slotItem.event,
                        timeFormatter = timeFormatter,
                        onLongClick = {onLongPressEvent(slotItem.event)},
                        onClick = { onEventClick(slotItem.event) },
                        isSelected = (slotItem.event.id == firstEvent?.id) || (slotItem.event.id == secondEvent?.id)
                    )
                }
                is ScheduleSlotItem.GapItem -> GapCard(
                    startTime = slotItem.startTime,
                    endTime = slotItem.endTime,
                    timeFormatter = timeFormatter
                )
            }
        }
    }
}

/**
 * Composable for displaying an individual event.
 *
 * @param event The [Event] to display.
 * @param timeFormatter Formatter for displaying times.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventCard(event: Event, timeFormatter: DateTimeFormatter, onClick: () -> Unit = {}, onLongClick: () -> Unit = {}, isSelected: Boolean) {
    var modifier = Modifier.fillMaxWidth().combinedClickable(
        onClick = onClick,
        onLongClick = onLongClick
    )

    if (isSelected){
        modifier = modifier.border(
            width = 2.dp,
            color = colorScheme.onSecondaryContainer,
            shape = MaterialTheme.shapes.medium
        )
    }

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
//            containerColor = event.color ?: MaterialTheme.colorScheme.primaryContainer
            containerColor = when (isSelected) {
                true -> colorScheme.secondaryContainer
                false -> colorScheme.primaryContainer
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = event.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Miejsce: ${event.place}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${event.startTime.format(timeFormatter)} - ${event.endTime.format(timeFormatter)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Composable for displaying a gap (free time slot) in the schedule.
 *
 * @param startTime The start time of the gap.
 * @param endTime The end time of the gap.
 * @param timeFormatter Formatter for displaying times.
 */
@Composable
fun GapCard(startTime: LocalTime, endTime: LocalTime, timeFormatter: DateTimeFormatter) {

    val outline = MaterialTheme.colorScheme.outline
    val stroke = Stroke(width = 2f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    )
    Card(
        modifier = Modifier.fillMaxWidth().drawBehind{
            drawRoundRect(color=outline, style = stroke, cornerRadius = CornerRadius(40f,40f))
        },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(
            width = 0.dp,
//            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Wolne",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${startTime.format(timeFormatter)} - ${endTime.format(timeFormatter)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}


@Preview(showBackground = true, name = "Daily Schedule Preview")
@Composable
fun DailyScheduleViewPreview() {
    val sampleEvents = listOf(
        Event(
            name = "Poranne spotkanie",
            place = "Biuro, Sala A",
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 30),
            color = Color(0xFFB2DFDB), // Light Teal
            eventId = 0
        ),
        Event(
            name = "Lunch z klientem",
            place = "Restauracja Centrum",
            startTime = LocalTime.of(12, 0),
            endTime = LocalTime.of(13, 30),
            color = Color(0xFFFFF9C4), // Light Yellow
            eventId = 0
        ),
        Event(
            name = "Prezentacja projektu",
            place = "Online",
            startTime = LocalTime.of(15, 0),
            endTime = LocalTime.of(16, 30),
            color = Color(0xFFC5CAE9), // Light Indigo
            eventId = 0
        ),
        Event(
            name = "Wieczorne zadania",
            place = "Dom",
            startTime = LocalTime.of(20, 0),
            endTime = LocalTime.of(21, 45),
            color = Color(0xFFD1C4E9), // Light Deep Purple
            eventId = 0
        )
    )
    val today = LocalDate.now()

    MaterialTheme { // Apply MaterialTheme for preview
        Surface(modifier = Modifier.fillMaxSize()) {
            DailyScheduleView(date = today, events = sampleEvents)
        }
    }
}

@Preview(showBackground = true, name = "Empty Day Schedule Preview")
@Composable
fun EmptyDailyScheduleViewPreview() {
    val today = LocalDate.now()
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            DailyScheduleView(date = today, events = emptyList())
        }
    }
}

@Preview(showBackground = true, name = "Schedule with Edge Case Times")
@Composable
fun EdgeCaseSchedulePreview() {
    val sampleEvents = listOf(
        Event(
            name = "Wczesny poranek",
            place = "Siłownia",
            startTime = LocalTime.MIN, // 00:00
            endTime = LocalTime.of(1, 30),
            eventId = 0
        ),
        Event(
            name = "Późna noc",
            place = "Dom",
            startTime = LocalTime.of(22, 0),
            endTime = LocalTime.MAX, // 23:59:59...
            eventId = 0
        )
    )
    val today = LocalDate.now()
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            DailyScheduleView(date = today, events = sampleEvents)
        }
    }
}
