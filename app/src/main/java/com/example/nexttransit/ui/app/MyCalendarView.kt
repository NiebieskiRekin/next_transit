package com.example.nexttransit.ui.app

import android.Manifest
import android.R.attr.text
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.Shape
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat.startActivity
import androidx.glance.action.actionStartActivity
import com.example.nexttransit.R
import com.example.nexttransit.api.ApiCaller
import com.example.nexttransit.model.calendar.CalendarInfo
import com.example.nexttransit.model.calendar.Event
import com.example.nexttransit.model.calendar.ScheduleSlotItem
import com.example.nexttransit.model.calendar.TZ
import com.example.nexttransit.model.calendar.generateScheduleSlots
import com.example.nexttransit.model.calendar.getAvailableCalendars
import com.example.nexttransit.model.calendar.getEvents
import com.example.nexttransit.model.database.classes.DepartAtOrArriveBy
import com.example.nexttransit.model.routes.DirectionsResponse
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalTime
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.time.YearMonth

val CHANNEL_ID = "TRANSIT_RESULT"

fun onEventClick(event: Event, context: Context) {
    // Edit event in system calendar
    val uri: Uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.eventId)
    val intent = Intent(Intent.ACTION_EDIT)
        .setData(uri).putExtra(CalendarContract.Events.TITLE, event.name)
    startActivity(context, intent, null)
}

@Composable
fun DoubleEvent(e1: Event, e2: Event) {
    val timeFormatter = remember {
        LocalTime.Format {
            hour(); char(':'); minute()
        }
    }
    val context = LocalContext.current

    Row(modifier = Modifier.fillMaxWidth()) {
        EventCard(
            modifier = Modifier
                .weight(1f)
                .height(100.dp),
            event = e1,
            timeFormatter = timeFormatter,
            onClick = { onEventClick(e1, context) },
            isSelected = false
        )
        EventCard(
            modifier = Modifier
                .weight(1f)
                .height(100.dp),
            event = e2,
            timeFormatter = timeFormatter,
            onClick = { onEventClick(e2, context) },
            isSelected = false
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MyCalendarView(
    contentResolver: ContentResolver,
    createNotification: suspend (event1: Event, event2: Event, directions: DirectionsResponse, departAtOrArriveBy: DepartAtOrArriveBy) -> Unit
) {
    val scope = rememberCoroutineScope()
    // In your Activity or Composable
    var events = remember { mutableStateListOf<Event>() }
    var calendar by remember { mutableStateOf<CalendarInfo?>(null) }
    val calendars = remember { mutableStateListOf<CalendarInfo>() }

    val requestCalendarPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. You can proceed to fetch calendars/events.
            Log.d("CalendarAccess", "READ_CALENDAR permission granted.")
            // Launch your calendar fetching logic here
            calendars.clear()
            calendars.addAll(getAvailableCalendars(contentResolver))
            Log.d("CalendarAccess", calendars.toString())
        } else {
            // Permission denied. Handle appropriately (e.g., show a message).
            Log.w("CalendarAccess", "READ_CALENDAR permission denied.")
            // Inform the user why the permission is needed
            events.clear()
        }
    }

    var showDialog by remember { mutableStateOf(true) };
    var selectedDate by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TZ).date) }
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }

    fun onDismissRequest() {
        events.clear()
        Log.d("CalendarAccess", "Brak dostępu do kalendarza")
        showDialog = false;
    }

    fun onDateChanged() {
        if (calendar != null) {
            events.clear()
            val dayStart = selectedDate.atStartOfDayIn(TZ)
            val dayEnd = selectedDate.atTime(23, 59, 59, 999_999_999).toInstant(TZ)
            events.addAll(
                getEvents(
                    contentResolver,
                    calendar!!.id,
                    dayStart.toEpochMilliseconds(),
                    dayEnd.toEpochMilliseconds()
                ).toMutableStateList()
            )
            Log.d("CalendarAccess", events.toString())
        }
    }

    val midnightStart = LocalTime(0, 0, 0, 0)
    val midnightEnd = LocalTime(23, 59, 59, 999_999_999)
    var scheduleSlots = remember(events.toList(), midnightStart, midnightEnd) {
        generateScheduleSlots(events, midnightStart, midnightEnd)
    }

    val timeFormatter = remember {
        LocalTime.Format {
            hour(); char(':'); minute()
        }
    }

    if (scheduleSlots.isEmpty()) {
        // Handle empty day - could show a full day gap or a message
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GapCard(startTime = midnightStart, endTime = midnightEnd, timeFormatter = timeFormatter)
        }
        return
    }

    var firstEvent by remember { mutableStateOf<Event?>(null) }
    var secondEvent by remember { mutableStateOf<Event?>(null) }
    val context = LocalContext.current


    fun onLongPressEvent(event: Event) {
        Log.d("CalendarAccess", firstEvent.toString() + "\n" + secondEvent.toString())
        if (firstEvent?.id == event.id) {
            firstEvent = null
            return
        }

        if (secondEvent?.id == event.id) {
            secondEvent = null
            return
        }

        if (firstEvent == null) {
            firstEvent = event
        } else if (secondEvent == null) {
            secondEvent = event
        }
    }


    Scaffold(
        floatingActionButton = {
            if (firstEvent != null && secondEvent != null) {
                Column {
                    SmallFloatingActionButton(
                        onClick = {
                            scope.launch {
                                if (firstEvent!!.endDateTime > secondEvent!!.endDateTime) {
                                    val temp = firstEvent
                                    firstEvent = secondEvent
                                    secondEvent = temp
                                }
                                var departureDateTime = firstEvent!!.endDateTime
                                try {
                                    val directions = ApiCaller.getDirectionsByNameAndDepartAt(
                                        firstEvent!!.place, secondEvent!!.place, departureDateTime
                                    )
                                    Log.d("CalendarAccess", directions.toString())

                                    createNotification(
                                        firstEvent!!,
                                        secondEvent!!,
                                        directions,
                                        DepartAtOrArriveBy.DepartAt
                                    )
                                } catch (e: Exception) {
                                    Log.d("CalendarAccess", e.message.toString())
                                    e.printStackTrace()
                                    Toast.makeText(
                                        context,
                                        "Nie udało się znaleźć trasy",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .size(80.dp)
                            .padding(4.dp)
                    ) {
                        Icon(
                            painterResource(R.drawable.depart_at),
                            "Wyznacz trasę wyruszając od końca pierwszego zdarzenia",
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                    SmallFloatingActionButton(
                        onClick = {
                            scope.launch {
                                if (firstEvent!!.startDateTime > secondEvent!!.startDateTime) {
                                    val temp = firstEvent
                                    firstEvent = secondEvent
                                    secondEvent = temp
                                }
                                var arrivalDateTime = secondEvent!!.startDateTime

                                try {
                                    val directions = ApiCaller.getDirectionsByNameAndArriveBy(
                                        firstEvent!!.place, secondEvent!!.place, arrivalDateTime
                                    )
                                    Log.d("CalendarAccess", directions.toString())

                                    createNotification(
                                        firstEvent!!,
                                        secondEvent!!,
                                        directions,
                                        DepartAtOrArriveBy.ArriveBy
                                    )
                                } catch (e: Exception) {
                                    Log.d("CalendarAccess", e.message.toString())
                                    e.printStackTrace()
                                    Toast.makeText(
                                        context,
                                        "Nie udało się znaleźć trasy",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .size(80.dp)
                            .padding(4.dp)
                    ) {
                        Icon(
                            painterResource(R.drawable.arrive_by),
                            "Wyznacz trasę, aby dotrzeć na czas na drugie zdarzenie",
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            } else {
                // Nowy przycisk, zawsze widoczny, do dodania eventu w wybranym kalendarzu
                SmallFloatingActionButton(
                    onClick = {
                        calendar?.let {
                            val intent = Intent(Intent.ACTION_INSERT).apply {
                                data = CalendarContract.Events.CONTENT_URI
                                putExtra(CalendarContract.Events.CALENDAR_ID, it.id)
                                putExtra(CalendarContract.Events.TITLE, "Nowe wydarzenie")
                            }
                            context.startActivity(intent)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(16.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.baseline_event_24),
                        contentDescription = "Dodaj nowe wydarzenie do kalendarza"
                    )
                }
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    // Header section with month/year and navigation buttons
                    CalendarHeader(
                        yearMonth = currentYearMonth,
                        onPreviousMonthClicked = {
                            currentYearMonth = currentYearMonth.minusMonths(1)
//                            selectedDate = null;
                        },
                        onNextMonthClicked = {
                            currentYearMonth = currentYearMonth.plusMonths(1)
//                            selectedDate = null;
                        },
                        selectedDay = selectedDate
                    )
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                )
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 8.dp)
        ) {
            item {
                SimpleCalendarView(
                    onDateSelected = { date ->
                        selectedDate = date
                        onDateChanged()
                        Log.d("MainActivity", "Selected date: $date")
                    },
                    currentYearMonth = currentYearMonth,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(8.dp)
                )
            }

            itemsIndexed(scheduleSlots) { i, slotItem ->
                when (slotItem) {
                    is ScheduleSlotItem.EventItem -> {
                        EventCard(
                            modifier = Modifier.fillMaxWidth(),
                            event = slotItem.event,
                            timeFormatter = timeFormatter,
                            onLongClick = { onLongPressEvent(slotItem.event) },
                            onClick = { onEventClick(slotItem.event, context) },
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
        if (showDialog) {
            Dialog(onDismissRequest = { onDismissRequest() }) {
                Card(
                    modifier = Modifier
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(8.dp)
                    ) {
                        if (calendars.isEmpty()) {
                            Text(
                                "Ta funkcjonalność aplikacji wymaga dostępu do kalendarza",
                                modifier = Modifier.padding(8.dp)
                            )
                            Row {
                                TextButton(
                                    onClick = { onDismissRequest() },
                                    modifier = Modifier.padding(8.dp),
                                ) {
                                    Text("Odrzuć", textAlign = TextAlign.Center)
                                }
                                TextButton(
                                    onClick = {
                                        requestCalendarPermissionLauncher.launch(
                                            Manifest.permission.READ_CALENDAR
                                        )
                                    },
                                    modifier = Modifier.padding(8.dp),
                                ) {
                                    Text(
                                        "Zezwól na dostęp",
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            Text("Wybierz kalendarz z listy")

                            // Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
                            LazyColumn(
                                Modifier.selectableGroup(),
                                contentPadding = PaddingValues(8.dp)
                            ) {
                                itemsIndexed(calendars) { i, c ->
                                    val modifierCal = Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = (calendar == c),
                                            onClick = {
                                                calendar = c; onDateChanged(); showDialog =
                                                false;
                                            },
                                            role = Role.RadioButton
                                        )

                                    Row(
                                        modifier = modifierCal,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = (c == calendar),
                                            onClick = null,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (c.color != null) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .clip(CircleShape)
                                                            .background(c.color)
                                                    )
                                                }
                                                Text(
                                                    text = c.displayName,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    modifier = Modifier.padding(4.dp)
                                                )
                                            }
                                            Text(
                                                text = c.accountName,
                                                overflow = TextOverflow.Ellipsis,
                                                maxLines = 1,
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                            Spacer(Modifier.padding(8.dp))
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}