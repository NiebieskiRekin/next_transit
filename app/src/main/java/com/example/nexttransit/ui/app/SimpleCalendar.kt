package com.example.nexttransit.ui.app

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexttransit.model.calendar.TZ
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toLocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.ceil

/**
 * Main composable function for the simple calendar view.
 * It manages the state of the currently displayed month and handles navigation.
 *
 * @param modifier Modifier for this composable.
 * @param currentYearMonth The initial month and year to display. Defaults to the current month.
 * @param onDateSelected Callback function that is invoked when a date is selected.
 * The LocalDate object for the selected date is passed as an argument.
 * Can be null if no date selection handling is needed.
 */
@Composable
fun SimpleCalendarView(
    modifier: Modifier = Modifier,
    currentYearMonth: YearMonth,
    onDateSelected: ((LocalDate) -> Unit)? = null,
) {
    // State for the currently selected date
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val daysOfWeek = 7

    // Get the current system date to highlight it
    val today = Clock.System.now().toLocalDateTime(TZ).date

    // Generate the list of days to display in the grid
    val daysInMonth = currentYearMonth.lengthOfMonth()
    val firstOfMonth = currentYearMonth.atDay(1)
    // Calculate the number of empty cells before the first day of the month
    // DayOfWeek.MONDAY.value is 1, DayOfWeek.SUNDAY.value is 7
    val emptyCellsBefore = (firstOfMonth.dayOfWeek.value - DayOfWeek.MONDAY.value + daysOfWeek) % daysOfWeek

    // Create a list of nullable integers representing day numbers.
    // Null values are for empty cells in the grid.
    val calendarDays = List(emptyCellsBefore) { null } + (1..daysInMonth).toList()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Header for the days of the week (Mon, Tue, etc.)
        DaysOfWeekHeader()

        Spacer(modifier = Modifier.height(8.dp))

        // Grid for displaying the days of the month
        Column (
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            var rows = ceil(calendarDays.size / daysOfWeek.toFloat()).toInt()
            for (rowId in 0 until rows) {
                val firstIndex = rowId * daysOfWeek

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (columnId in 0 until daysOfWeek) {
                        Log.e("CalendarView", "rowId: $rowId, columnId: $columnId")
                        val index = firstIndex + columnId
                        val day = calendarDays.getOrNull(index)
                        if (day == null) {
                            // Empty cell, occupies space
                            Box(modifier = Modifier.aspectRatio(1f).weight(1f))
                        } else {
                            val date = currentYearMonth.atDay(day).toKotlinLocalDate()
                            CalendarDay(
                                day = day,
                                date = date,
                                isCurrentDay = date == today,
                                isSelected = (selectedDate != null) && date == selectedDate,
                                onDateClicked = { clickedDate ->
                                    selectedDate = clickedDate
                                    onDateSelected?.invoke(clickedDate)
                                },
                                Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Composable for the calendar header.
 * Displays the current month and year, and navigation buttons.
 *
 * @param yearMonth The current month and year.
 * @param onPreviousMonthClicked Callback for when the previous month button is clicked.
 * @param onNextMonthClicked Callback for when the next month button is clicked.
 */
@Composable
fun CalendarHeader(
    yearMonth: YearMonth,
    selectedDay: LocalDate? = null,
    onPreviousMonthClicked: () -> Unit,
    onNextMonthClicked: () -> Unit
) {
    // Formatter for the month and year string
    val monthYearFormatter = remember { DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault()) }
    val dayMonthYearFormatter = remember { DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPreviousMonthClicked) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Poprzedni miesiąc")
        }

        Text(
            text = selectedDay?.toJavaLocalDate()?.format(dayMonthYearFormatter) ?: yearMonth.format(monthYearFormatter).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = onNextMonthClicked) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Następny miesiąc")
        }
    }
}

/**
 * Composable for the header displaying the days of the week.
 * Uses short names for days (e.g., "Pn", "Wt").
 */
@Composable
fun DaysOfWeekHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround // Distributes space evenly
    ) {
        // Get DayOfWeek values (MONDAY to SUNDAY)
        val daysOfWeek = DayOfWeek.entries.toTypedArray()
        // Shift array so Monday is the first day
        val shiftedDaysOfWeek = daysOfWeek.drop(0) + daysOfWeek.take(0) // No shift needed if DayOfWeek.values() starts with MONDAY

        shiftedDaysOfWeek.forEach { dayOfWeek ->
            Text(
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f) // Each day takes equal width
            )
        }
    }
}

/**
 * Composable for an individual day cell in the calendar grid.
 *
 * @param day The day number (1-31).
 * @param date The LocalDate object for this day.
 * @param isCurrentDay True if this day is the current system date.
 * @param isSelected True if this day is currently selected.
 * @param onDateClicked Callback for when this day cell is clicked.
 */
@Composable
fun CalendarDay(
    day: Int,
    date: LocalDate,
    isCurrentDay: Boolean,
    isSelected: Boolean,
    onDateClicked: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isCurrentDay -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        else -> Color.Transparent
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isCurrentDay -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .aspectRatio(1f) // Makes the cell square
            .clip(MaterialTheme.shapes.small)
            .background(backgroundColor)
            .clickable { onDateClicked(date) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (isCurrentDay || isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

///**
// * Preview composable for SimpleCalendarView.
// * Allows you to see the calendar in Android Studio's preview.
// */
//@Preview(showBackground = true, name = "Simple Calendar View")
//@Composable
//fun SimpleCalendarViewPreview() {
//    MaterialTheme { // Ensure a MaterialTheme is applied for previews
//        SimpleCalendarView(
//            onDateSelected = { date ->
//                println("Selected date: $date")
//            }
//        )
//    }
//}
//
//@Preview(showBackground = true, name = "Simple Calendar View - Specific Month")
//@Composable
//fun SimpleCalendarViewSpecificMonthPreview() {
//    MaterialTheme {
//        SimpleCalendarView(initialYearMonth = YearMonth.of(2025, 12))
//    }
//}

