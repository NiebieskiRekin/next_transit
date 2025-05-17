package com.example.nexttransit.ui.app

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat.startActivity
import com.example.nexttransit.model.calendar.CalendarInfo
import com.example.nexttransit.model.calendar.Event
import com.example.nexttransit.model.calendar.ScheduleSlotItem
import com.example.nexttransit.model.calendar.generateScheduleSlots
import com.example.nexttransit.model.calendar.getAvailableCalendars
import com.example.nexttransit.model.calendar.getEvents
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MyCalendarView(contentResolver: ContentResolver) {
    // In your Activity or Composable
    var events = remember { mutableStateListOf<Event>() }
    var calendar by remember { mutableStateOf<CalendarInfo?>(null) }
    val calendars = remember { mutableStateListOf<CalendarInfo>() }
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. You can proceed to fetch calendars/events.
            Log.d("CalendarAccess", "READ_CALENDAR permission granted.")
            // Launch your calendar fetching logic here
            calendars.clear()
            calendars.addAll(getAvailableCalendars(contentResolver))
        } else {
            // Permission denied. Handle appropriately (e.g., show a message).
            Log.w("CalendarAccess", "READ_CALENDAR permission denied.")
            // Inform the user why the permission is needed
            events.clear()
        }
    }

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }

    fun onDismissRequest() {
        events.clear()
        Log.d("CalendarAccess", "No calendar chosen")
    }

    fun onDateChanged() {
        if (calendar != null) {
            events.clear()
            val startDateMillis =
                selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
                    .toEpochMilli()
            val endDateMillis = selectedDate.atTime(23, 59, 59)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            events.addAll(
                getEvents(
                    contentResolver,
                    calendar!!.id,
                    startDateMillis,
                    endDateMillis
                ).toMutableStateList()
            )
            Log.d("CalendarAccess", events.toString())
        }
    }

    val dayStart = LocalTime.MIN
    val dayEnd = LocalTime.MAX
    var scheduleSlots = remember(events.toList(), dayStart, dayEnd) {
        generateScheduleSlots(events, dayStart, dayEnd)
    }

    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    if (scheduleSlots.isEmpty()) {
        // Handle empty day - could show a full day gap or a message
        Column(
            modifier = Modifier
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


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Header section with month/year and navigation buttons
                    CalendarHeader(
                        yearMonth = currentYearMonth,
                        onPreviousMonthClicked = {
                            currentYearMonth = currentYearMonth.minusMonths(1)
                            selectedDate = null;
                        },
                        onNextMonthClicked = {
                            currentYearMonth = currentYearMonth.plusMonths(1)
                            selectedDate = null;
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
        LazyColumn (
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(0.dp,0.dp,0.dp,8.dp)
        ){
            item{
                SimpleCalendarView(onDateSelected = { date ->
                    selectedDate = date
                    onDateChanged()
                    Log.d("MainActivity", "Selected date: $date")
                }, currentYearMonth = currentYearMonth, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer).padding(8.dp))
            }

            itemsIndexed(scheduleSlots){ i, slotItem ->
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

    if (calendar == null) {
        Dialog(onDismissRequest = { onDismissRequest() }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(375.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (calendars.isEmpty()) {
                        Text("No calendars available")
                        Row {
                            TextButton(
                                onClick = { onDismissRequest() },
                                modifier = Modifier.padding(8.dp),
                            ) {
                                Text("Dismiss", textAlign = TextAlign.Center)
                            }
                            TextButton(
                                onClick = {
                                    requestPermissionLauncher.launch(
                                        Manifest.permission.READ_CALENDAR
                                    )
                                },
                                modifier = Modifier.padding(8.dp),
                            ) {
                                Text(
                                    "Grant access to calendars",
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Text("Please choose a calendar")

                        // Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
                        LazyColumn(Modifier.selectableGroup()) {
                            itemsIndexed(calendars) { i, c ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .selectable(
                                            selected = (calendar == c),
                                            onClick = {
                                                calendar = c; onDateChanged()
                                            },
                                            role = Role.RadioButton
                                        )
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (c == calendar),
                                        onClick = null
                                    )
                                    Text(
                                        text = c.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(start = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}