package com.example.nexttransit

import android.Manifest
import android.R.attr.text
import android.appwidget.AppWidgetManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.sharp.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.nexttransit.api.ApiCaller
import com.example.nexttransit.api.ApiCaller.getSampleDirections
import com.example.nexttransit.api.ApiCaller.trimPolyline
import com.example.nexttransit.model.AppScreen
import com.example.nexttransit.model.calendar.CalendarInfo
import com.example.nexttransit.model.calendar.getAvailableCalendars
import com.example.nexttransit.model.calendar.getEvents
import com.example.nexttransit.model.calendar.getEventsForNext14Days
import com.example.nexttransit.model.routes.DirectionsResponse
import com.example.nexttransit.model.routes.Location
import com.example.nexttransit.model.settings.AppSettings
import com.example.nexttransit.model.settings.AppSettingsSerializer
import com.example.nexttransit.ui.app.DailyScheduleView
import com.example.nexttransit.ui.app.Event
import com.example.nexttransit.ui.app.LoadingDirectionsWidget
import com.example.nexttransit.ui.app.SimpleCalendarView
import com.example.nexttransit.ui.theme.NextTransitTheme
import com.example.nexttransit.ui.widget.TransitWidget
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId


class MainActivity : ComponentActivity() {
    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }
                // Get new FCM registration token
                val token = task.result

                // Log and toast
                Log.d(TAG, token)
                Toast.makeText(baseContext, token, Toast.LENGTH_SHORT).show()
            })
        } else {
            // Inform user that that your app will not show notifications.
            Log.d(TAG, "Permissions to send notifications refused")
        }
    }

    companion object {
        val Context.appSettingsDataStore: DataStore<AppSettings> by dataStore(
            "app-settings.json",
            serializer = AppSettingsSerializer,
        )
    }

    private suspend fun updateSettings(
        sourceName: String, destinationName: String, directions: DirectionsResponse
    ) {
        appSettingsDataStore.updateData {
            it.copy(
                source = Location(sourceName, directions.geocodedWaypoints[0].placeId),
                destination = Location(destinationName, directions.geocodedWaypoints[1].placeId),
                lastDirectionsResponse = directions
            )
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId: Int = extractAppWidgetId()
        val resultValue = createResultIntent(appWidgetId)
        setResultBasedOnWidgetId(appWidgetId, resultValue)

        setContent {
            NextTransitTheme {
                MainContent()
            }
        }
    }

    private fun extractAppWidgetId(): Int {
        return intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }

    private fun createResultIntent(appWidgetId: Int): Intent {
        return Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    }

    private fun setResultBasedOnWidgetId(appWidgetId: Int, resultValue: Intent) {
        when (appWidgetId) {
            AppWidgetManager.INVALID_APPWIDGET_ID -> {
                setResult(RESULT_CANCELED, resultValue)
            }

            else -> {
                setResult(RESULT_OK, resultValue)
            }
        }
    }



    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainContent(
        navController: NavHostController = rememberNavController()
    ) {
        var currentDestination by rememberSaveable { mutableStateOf(AppScreen.Start) }
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                AppScreen.entries.forEach {
                    item(
                        icon = { Icon(it.icon, contentDescription = stringResource(it.contentDescription)) },
                        label = { Text(stringResource(it.title)) },
                        selected = currentDestination == it,
                        onClick = { currentDestination = it }
                    )
                }
            },
            Modifier.fillMaxSize()
        ) {
            when (currentDestination) {
                AppScreen.Notifications -> Text("Notifications")
                AppScreen.Start -> Text("Start")
                AppScreen.Calendar -> {
                    // In your Activity or Composable
                    var events = remember { mutableStateListOf<Event>() }
                    var calendar by remember { mutableStateOf<CalendarInfo?>(null) }
                    val calendars = remember {mutableStateListOf<CalendarInfo>()}
                    val requestPermissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { isGranted: Boolean ->
                        if (isGranted) {
                            // Permission is granted. You can proceed to fetch calendars/events.
                            Log.d("CalendarAccess", "READ_CALENDAR permission granted.")
                            // Launch your calendar fetching logic here
                            calendars.clear()
                            calendars.addAll( getAvailableCalendars(contentResolver))
                        } else {
                            // Permission denied. Handle appropriately (e.g., show a message).
                            Log.w("CalendarAccess", "READ_CALENDAR permission denied.")
                            // Inform the user why the permission is needed

                            events.clear()
//                            events.addAll(listOf(
//                                Event(
//                                    name = "Poranne spotkanie",
//                                    place = "Biuro, Sala A",
//                                    startTime = LocalTime.of(9, 0),
//                                    endTime = LocalTime.of(10, 30),
//                                    color = Color(0xFFB2DFDB) // Light Teal
//                                ),
//                                Event(
//                                    name = "Lunch z klientem",
//                                    place = "Restauracja Centrum",
//                                    startTime = LocalTime.of(12, 0),
//                                    endTime = LocalTime.of(13, 30),
//                                    color = Color(0xFFFFF9C4) // Light Yellow
//                                ),
//                                Event(
//                                    name = "Prezentacja projektu",
//                                    place = "Online",
//                                    startTime = LocalTime.of(15, 0),
//                                    endTime = LocalTime.of(16, 30),
//                                    color = Color(0xFFC5CAE9) // Light Indigo
//                                ),
//                                Event(
//                                    name = "Wieczorne zadania",
//                                    place = "Dom",
//                                    startTime = LocalTime.of(20, 0),
//                                    endTime = LocalTime.of(21, 45),
//                                    color = Color(0xFFD1C4E9) // Light Deep Purple
//                                )
//                            ))
                        }
                    }

                    val scrollBehavior =
                        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState()) // https://developer.android.com/develop/ui/compose/components/app-bars#scroll
                    var selectedDate by remember{mutableStateOf(LocalDate.now())}

                    fun onDismissRequest(){
                        events.clear()
                        Log.d("CalendarAccess", "No calendar chosen")
                    }

                    fun onDateChanged(){
                        if (calendar != null){
                            events.clear()
                            val startDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                            val endDateMillis = selectedDate.atTime(23, 59, 59)
                                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                            events.addAll(getEvents(contentResolver, calendar!!.id,startDateMillis, endDateMillis).toMutableStateList())
                            Log.d("CalendarAccess", events.toString())
                        }
                    }

                    Scaffold(topBar = {
                        LargeTopAppBar(
                            title = {
                                SimpleCalendarView(onDateSelected = {
                                    date ->
                                    selectedDate = date
                                    onDateChanged()
                                    Log.d("MainActivity", "Selected date: $date")
                                })
                            },
                            scrollBehavior = scrollBehavior,
                            expandedHeight = 400.dp
                        )
                    }, modifier=Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)){ innerPadding ->
                        DailyScheduleView(selectedDate,events.toList(),Modifier.padding(innerPadding).nestedScroll(scrollBehavior.nestedScrollConnection))
                    }

                    if (calendar == null){
                        Dialog(onDismissRequest = { onDismissRequest() }){
                            Card(            modifier = Modifier
                                .fillMaxWidth()
                                .height(375.dp)
                                .padding(16.dp),
                                shape = RoundedCornerShape(16.dp)
                            ){
                                Column(verticalArrangement = Arrangement.SpaceAround, horizontalAlignment = Alignment.CenterHorizontally, modifier=Modifier.fillMaxSize()) {
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
                                                Text("Grant access to calendars", textAlign = TextAlign.Center)
                                            }
                                        }
                                    } else {
                                        Text("Please choose a calendar")

                                        // Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
                                        LazyColumn (Modifier.selectableGroup()) {
                                            itemsIndexed(calendars) { i,c ->
                                                Row(
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .height(56.dp)
                                                        .selectable(
                                                            selected = (calendar == c),
                                                            onClick = { calendar = c; onDateChanged()},
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
                AppScreen.WidgetSettings -> {
                    Column{
                        Text("Widget Settings")
                        val appSettings = appSettingsDataStore.data.collectAsState(initial = AppSettings()).value
                        WidgetSettingsView(
                            it = PaddingValues(0.dp),
                            appSettings = appSettings,
                        )
                    }

                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AppBar(
        currentScreen: AppScreen,
        canNavigateBack: Boolean,
        navigateUp: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        TopAppBar(
            title = { Text(stringResource(currentScreen.title)) },
            colors = TopAppBarDefaults.mediumTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = modifier,
            navigationIcon = {
                if (canNavigateBack) {
                    IconButton(onClick = navigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button)
                        )
                    }
                }
            }
        )
    }

    @Composable
    fun WidgetSettingsView(it: PaddingValues, appSettings: AppSettings){
        var directions1 by remember { mutableStateOf(appSettings.lastDirectionsResponse) }
        var directions1ButtonClicked by remember { mutableStateOf(false) }
        var directions2 by remember { mutableStateOf(appSettings.returnResponse) }
        var source1 by remember { mutableStateOf(appSettings.source.name) }
        var destination1 by remember { mutableStateOf(appSettings.destination.name) }
        var directions2Generated by remember { mutableStateOf(false) }
        var directions1Generated by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        if (directions1Generated || directions2Generated) {
            FloatingActionButton(
                onClick = {
                    val resultValue: Intent = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, extractAppWidgetId())
                    setResult(RESULT_OK, resultValue)
                    val manager = GlanceAppWidgetManager(applicationContext)
                    val widget = TransitWidget()
                    scope.launch {
                        try {
                            widget.update(
                                applicationContext,
                                manager.getGlanceIdBy(extractAppWidgetId())
                            )
                            finish()
                        } catch (e: Exception) {
                            Log.e("TransitWidget", "Couldn't update widget. $e")
                        }
                    }
                },
            ) {
                Icon(
                    Icons.Default.Save, "Save and exit"
                )
            }
        }

        LazyColumn(Modifier.padding(it)) {
            item {
                DirectionsTextFieldsSettings(
                    source1,
                    destination1,
                    Pair(directions1Generated, directions1),
                    ::updateSettings,
                    { directions1ButtonClicked = it }
                ) { directionsGenerated, source, destination, directions ->
                    directions1Generated = directionsGenerated
                    source1 = source
                    destination1 = destination
                    directions1 = directions
                }
            }
            item {
                LoadingDirectionsWidget(
                    directions = directions1,
                    source = source1,
                    destination = destination1,
                    directionsButtonClicked = directions1ButtonClicked,
                    directionsGenerated = directions1Generated
                )
            }

            item {
                DebugOutput(
                    appSettings = appSettings,
                    newDirections1 = directions1,
                    newDirections2 = directions2
                )
            }
        }
    }


    @Composable
    fun DebugOutput(
        appSettings: AppSettings = AppSettings().getDefault(),
        newDirections1: DirectionsResponse = getSampleDirections(),
        newDirections2: DirectionsResponse = DirectionsResponse()
    ) {
        var showDebugOutput by remember { mutableStateOf(false) }
        Column {
            TextButton(onClick = { showDebugOutput = !showDebugOutput }, content = {
                Text(
                    if (showDebugOutput) {
                        "Hide Debug Output"
                    } else {
                        "Show Debug Output"
                    }
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Sharp.ChevronRight,
                    contentDescription = ">",
                    Modifier.rotate(
                        if (showDebugOutput) {
                            90f
                        } else {
                            0f
                        }
                    )
                )
            },
                modifier = Modifier.padding(5.dp, 0.dp)
            )
            if (showDebugOutput) {
                LazyColumn(
                    modifier = Modifier
                        .padding(5.dp)
                        .border(2.dp, Color.LightGray)
                        .padding(5.dp)
                        .height(700.dp)
                ) {
                    item {
                        Text(
                            text = "Saved origin 1:",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                        SelectionContainer {
                            Text(text = appSettings.source.toString())
                        }
                    }
                    item {
                        Spacer(
                            Modifier
                                .size(0.dp, 5.dp)
                                .fillMaxWidth()
                        )
                    }
                    item {
                        Text(
                            text = "Saved destination 1:",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                        SelectionContainer {
                            Text(text = appSettings.destination.toString())
                        }
                    }
                    item {
                        Spacer(
                            Modifier
                                .size(0.dp, 5.dp)
                                .fillMaxWidth()
                        )
                    }
                    item {
                        Text(
                            text = "Saved last directions response 1:",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                        SelectionContainer {
                            Text(text = trimPolyline(appSettings.lastDirectionsResponse).toString())
                        }
                    }
                    item {
                        Spacer(
                            Modifier
                                .size(0.dp, 10.dp)
                                .fillMaxWidth()
                        )
                    }
                    item {
                        Text(
                            text = "New Directions 1:",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                        SelectionContainer {
                            Text(text = trimPolyline(newDirections1).toString())
                        }
                    }
                    item {
                        Spacer(
                            Modifier
                                .size(0.dp, 15.dp)
                                .fillMaxWidth()
                        )
                    }
                    item {
                        Text(
                            text = "Saved origin 2:",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                        SelectionContainer {
                            Text(text = appSettings.secondSource.toString())
                        }
                    }
                    item {
                        Spacer(
                            Modifier
                                .size(0.dp, 5.dp)
                                .fillMaxWidth()
                        )
                    }
                    item {
                        Text(
                            text = "Saved destination 2:",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                        SelectionContainer {
                            Text(text = appSettings.secondDestination.toString())
                        }
                    }
                    item {
                        Spacer(
                            Modifier
                                .size(0.dp, 5.dp)
                                .fillMaxWidth()
                        )
                    }
                    item {
                        Text(
                            text = "Saved last directions response 2:",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                        SelectionContainer {
                            Text(text = trimPolyline(appSettings.returnResponse).toString())
                        }
                    }
                    item {
                        Spacer(
                            Modifier
                                .size(0.dp, 10.dp)
                                .fillMaxWidth()
                        )
                    }
                    item {
                        Text(
                            text = "New Directions 2:",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                        SelectionContainer {
                            Text(text = trimPolyline(newDirections2).toString())
                        }
                    }

                }
            }
        }
    }

    private suspend fun getDirections(
        source: String,
        destination: String
    ): Pair<Boolean, DirectionsResponse> {
        return try {
            Pair(true, ApiCaller.getDirectionsByName(source, destination))
        } catch (_: Exception) {
            Pair(false, DirectionsResponse(status = "Error"))
        }
    }

    @Composable
    fun DirectionsTextFieldsSettings(
        sourceSeed: String = "Poznań",
        destinationSeed: String = "Kraków",
        directions: Pair<Boolean, DirectionsResponse> = Pair(false, getSampleDirections()),
        update: suspend (String, String, DirectionsResponse) -> Unit = ::updateSettings,
        onGetDirectionsButtonClicked: (getDirectionsButtonClicked: Boolean) -> Unit,
        onDirectionsGet: (directionsGenerated: Boolean, source: String, destination: String, directions: DirectionsResponse) -> Unit
    ) {
        var source by remember { mutableStateOf(TextFieldValue(sourceSeed)) }
        var destination by remember { mutableStateOf(TextFieldValue(destinationSeed)) }
        var prefsButtonText by remember { mutableStateOf("Update preferences") }
        val scope = rememberCoroutineScope()

        // super weird
        val tertiary = MaterialTheme.colorScheme.tertiary
        val secondary = MaterialTheme.colorScheme.secondary
        val error = MaterialTheme.colorScheme.error
        var prefsButtonColor by remember { mutableStateOf(tertiary) }

        fun onTextFieldValueChange() {
            prefsButtonColor = tertiary
            prefsButtonText = "Update preferences"
            onGetDirectionsButtonClicked(false)
            onDirectionsGet(false, source.text, destination.text, DirectionsResponse())
        }
        Column {

            OutlinedTextField(
                value = source,
                onValueChange = {
                    source = it
                    onTextFieldValueChange()
                },
                label = { Text("Origin") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp, 0.dp)
            )
            OutlinedTextField(
                value = destination,
                onValueChange = {
                    destination = it
                    onTextFieldValueChange()
                },
                label = { Text("Destination") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp, 0.dp)
            )

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = {
                    onGetDirectionsButtonClicked(true)
                    scope.launch {
                        val result = getDirections(source.text, destination.text)
                        onDirectionsGet(result.first, source.text, destination.text, result.second)
                    }
                }) {
                    Text("Show directions!")
                }
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                update(source.text, destination.text, directions.second)
                                prefsButtonText = "Updated"
                                prefsButtonColor = secondary
                            } catch (_: Exception) {
                                prefsButtonText = "Error!"
                                prefsButtonColor = error
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(prefsButtonColor),
                    enabled = source.text.isNotBlank() && destination.text.isNotBlank() && directions.first

                ) {
                    Text(prefsButtonText)
                }

            }
        }
    }


    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
                val msg = "Permissons granted to send notifications"
                Log.d(TAG, msg)
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.

            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

}

