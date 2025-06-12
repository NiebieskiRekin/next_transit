package com.example.nexttransit.ui.app

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.nexttransit.model.calendar.Event
import com.example.nexttransit.model.calendar.TZ
import com.example.nexttransit.model.calendar.getLocalTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Composable for displaying an individual event.
 *
 * @param event The [Event] to display.
 * @param timeFormatter Formatter for displaying times.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventCard(
    modifier: Modifier,
    event: Event,
    timeFormatter: DateTimeFormat<LocalTime>,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    isSelected: Boolean
) {
    var modifier = modifier
        .combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        )
        .padding(8.dp, 0.dp)

    val date_formatter = LocalDate.Format {
        dayOfMonth(); char('-'); monthNumber(); char('-'); year();
    }

    if (isSelected) {
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
                color = colorScheme.onPrimaryContainer,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = event.place,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${
                    event.startDateTime.getLocalTime(TZ).format(timeFormatter)
                } - ${event.endDateTime.getLocalTime(TZ).format(timeFormatter)}",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                maxLines = 1, overflow = TextOverflow.Ellipsis
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
fun GapCard(startTime: LocalTime, endTime: LocalTime, timeFormatter: DateTimeFormat<LocalTime>) {

    val outline = colorScheme.outline
    val stroke = Stroke(
        width = 4f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 0.dp)
            .drawBehind {
                drawRoundRect(
                    color = outline,
                    style = stroke,
                    cornerRadius = CornerRadius(16.dp.toPx())
                )
            }
            .clip(RoundedCornerShape(16.dp)),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.background
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
                color = colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${startTime.format(timeFormatter)} - ${endTime.format(timeFormatter)}",
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

//
//@Preview(showBackground = true, name = "Daily Schedule Preview")
//@Composable
//fun DailyScheduleViewPreview() {
//    val sampleEvents = listOf(
//        Event(
//            name = "Poranne spotkanie",
//            place = "Biuro, Sala A",
//            startTime = LocalTime.of(9, 0),
//            endTime = LocalTime.of(10, 30),
//            color = Color(0xFFB2DFDB), // Light Teal
//            eventId = 0
//        ),
//        Event(
//            name = "Lunch z klientem",
//            place = "Restauracja Centrum",
//            startTime = LocalTime.of(12, 0),
//            endTime = LocalTime.of(13, 30),
//            color = Color(0xFFFFF9C4), // Light Yellow
//            eventId = 0
//        ),
//        Event(
//            name = "Prezentacja projektu",
//            place = "Online",
//            startTime = LocalTime.of(15, 0),
//            endTime = LocalTime.of(16, 30),
//            color = Color(0xFFC5CAE9), // Light Indigo
//            eventId = 0
//        ),
//        Event(
//            name = "Wieczorne zadania",
//            place = "Dom",
//            startTime = LocalTime.of(20, 0),
//            endTime = LocalTime.of(21, 45),
//            color = Color(0xFFD1C4E9), // Light Deep Purple
//            eventId = 0
//        )
//    )
//    val today = LocalDate.now()
//
//    MaterialTheme { // Apply MaterialTheme for preview
//        Surface(modifier = Modifier.fillMaxSize()) {
////            DailyScheduleView(date = today, events = sampleEvents)
//        }
//    }
//}
//
//@Preview(showBackground = true, name = "Empty Day Schedule Preview")
//@Composable
//fun EmptyDailyScheduleViewPreview() {
//    val today = LocalDate.now()
//    MaterialTheme {
//        Surface(modifier = Modifier.fillMaxSize()) {
////            DailyScheduleView(date = today, events = emptyList())
//        }
//    }
//}
//
//@Preview(showBackground = true, name = "Schedule with Edge Case Times")
//@Composable
//fun EdgeCaseSchedulePreview() {
//    val sampleEvents = listOf(
//        Event(
//            name = "Wczesny poranek",
//            place = "Siłownia",
//            startTime = LocalTime.MIN, // 00:00
//            endTime = LocalTime.of(1, 30),
//            eventId = 0
//        ),
//        Event(
//            name = "Późna noc",
//            place = "Dom",
//            startTime = LocalTime.of(22, 0),
//            endTime = LocalTime.MAX, // 23:59:59...
//            eventId = 0
//        )
//    )
//    val today = LocalDate.now()
//    MaterialTheme {
//        Surface(modifier = Modifier.fillMaxSize()) {
////            DailyScheduleView(date = today, events = sampleEvents)
//        }
//    }
//}
