package com.example.nexttransit

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsBike
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DirectionsBoat
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.DirectionsRailway
import androidx.compose.material.icons.rounded.DirectionsTransit
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material.icons.rounded.Tram
import androidx.compose.material.icons.sharp.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.example.nexttransit.ApiCaller.getSampleDirections
import com.example.nexttransit.ApiCaller.trimPolyline
import com.example.nexttransit.ui.theme.NextTransitTheme
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale


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
            Log.d(TAG, "Permissions to send notifications refused");
        }
    }

    companion object {
        val Context.appSettingsDataStore: DataStore<AppSettings> by dataStore(
            "app-settings.json",
            serializer = AppSettingsSerializer,
        )
    }

    private val appWidgetId = intent?.extras?.getInt(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID
    ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

    private var resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED, resultValue)
        setContent {
            NextTransitTheme {
                val appSettings =
                    appSettingsDataStore.data.collectAsState(initial = AppSettings()).value
                MainContent(appSettings)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    fun MainContent(appSettings: AppSettings = AppSettings().getDefault()) {
        var directions1 by remember { mutableStateOf(appSettings.lastDirectionsResponse) }
        var directions1ButtonClicked by remember { mutableStateOf(false) }
        var directions1Generated by remember { mutableStateOf(false) }
        var directions2 by remember { mutableStateOf(appSettings.returnResponse) }
        var directions2ButtonClicked by remember { mutableStateOf(false) }
        var directions2Generated by remember { mutableStateOf(false) }
        var source1 by remember { mutableStateOf(appSettings.source.name) }
        var source2 by remember { mutableStateOf(appSettings.secondSource.name) }
        var destination1 by remember { mutableStateOf(appSettings.destination.name) }
        var destination2 by remember { mutableStateOf(appSettings.secondDestination.name) }

        val scope = rememberCoroutineScope()

        Scaffold(
            Modifier.fillMaxSize(),
            {
                TopAppBar(title = { Text("Next Transit") },
                    navigationIcon = {})
            }, {}, floatingActionButton = {
                if (directions1Generated || directions2Generated) {
                    IconButton(
                        onClick = {
                            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            setResult(RESULT_OK, resultValue)
                            val manager = GlanceAppWidgetManager(applicationContext)
                            val widget = TransitWidget()
                            scope.launch {
                                try {
                                    widget.update(
                                        applicationContext,
                                        manager.getGlanceIdBy(appWidgetId)
                                    )
                                    finish()
                                } catch (e: Exception) {
                                    Log.e("TransitWidget", "Couldn't update widget.")
                                }
                            }
                        },
                        colors = IconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceDim,
                            disabledContentColor = MaterialTheme.colorScheme.surfaceDim
                        )
                    ) {
                        Icon(
                            Icons.Default.Save, "Save and exit"
                        )
                    }
                }
            }
        ) { it ->
            LazyColumn(Modifier.padding(it)) {
                item {
                    CopyFirebaseToken()
                    Text(
                        "First Route:",
                        Modifier
                            .fillMaxWidth()
                            .padding(10.dp, 0.dp)
                    )
                    Content(
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
                    Spacer(Modifier.padding(10.dp))
                }


                item {
                    Text(
                        "Second Route (return of first by default):",
                        Modifier
                            .fillMaxWidth()
                            .padding(10.dp, 0.dp)
                    )

                    Content(
                        source2,
                        destination2,
                        Pair(directions2Generated, directions2),
                        ::updateSettings2,
                        { directions2ButtonClicked = it }
                    ) { directionsGenerated, source, destination, directions ->
                        directions2Generated = directionsGenerated
                        source2 = source
                        destination2 = destination
                        directions2 = directions
                    }
                }

                item {
                    LoadingDirectionsWidget(
                        directions = directions2,
                        source = source2,
                        destination = destination2,
                        directionsButtonClicked = directions2ButtonClicked,
                        directionsGenerated = directions2Generated
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
    }

    @Preview
    @Composable
    fun LoadingDirectionsWidget(
        directions: DirectionsResponse = getSampleDirections(),
        source: String = "Środa Wlkp.",
        destination: String = "Poznań, Piotrowo 2",
        directionsButtonClicked: Boolean = true,
        directionsGenerated: Boolean = false
    ) {
        if (!directionsGenerated && directionsButtonClicked) {
            ColumnPill(Modifier.height(80.dp)) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onSecondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        } else if (directionsGenerated) {
            SimpleDisplay(
                directions,
                source,
                destination
            )
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
        } catch (e: Exception) {
            Pair(false, DirectionsResponse(status = "Error"))
        }
    }

    @Composable
    fun Content(
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
                            } catch (e: Exception) {
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

    private suspend fun updateSettings2(
        sourceName2: String, destinationName2: String, directions2: DirectionsResponse
    ) {
        appSettingsDataStore.updateData {
            it.copy(
                secondSource = Location(sourceName2, directions2.geocodedWaypoints[0].placeId),
                secondDestination = Location(
                    destinationName2,
                    directions2.geocodedWaypoints[1].placeId
                ),
                returnResponse = directions2
            )
        }
    }

    @Composable
    fun ColumnPill(modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.secondary)
                .padding(10.dp)
        ) {
            content()
        }
    }

    @Composable
    fun ShowError(text: String) {
        ColumnPill(modifier = Modifier.height(120.dp)) {
            Text(
                "Error: ",
                fontWeight = FontWeight.Bold,
            )
            Text(
                text,
            )
        }
    }

    @Preview(showBackground = true)
    @Composable
    private fun SimpleDisplay(
        directions: DirectionsResponse = getSampleDirections(),
        source: String = "Środa Wlkp.",
        destination: String = "Poznań"
    ) {
        NextTransitTheme {
            when (directions.status) {
                "OK" -> {
                    ColumnPill(
                        modifier = Modifier.clickable(true, "Open Google Maps", null, onClick =
                        {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                ("https://www.google.com/maps/dir/?api=1" +
                                        "&origin=${source}" +
                                        "&destination=${destination}" +
                                        "&travelmode=transit").toUri()
                            )
                            ContextCompat.startActivity(this@MainActivity, intent, null)
                        })
                    ) {
                        if (directions.routes.isEmpty()) {
                            ShowError(text = "No route found.")
                        }
                        directions.routes.forEach { route: Route ->
                            route.legs.forEach { leg: Leg ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Departure: ",
                                        style = TextStyle(
                                            color = MaterialTheme.colorScheme.onSecondary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    )
                                    Text(
                                        text = leg.departureTime.text,
                                        style = TextStyle(
                                            color = MaterialTheme.colorScheme.onSecondary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    )
                                    Spacer(Modifier.size(16.dp))
                                    Text(
                                        text = "Planned Arrival: ",
                                        style = TextStyle(
                                            color = MaterialTheme.colorScheme.onSecondary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    )
                                    Text(
                                        text = leg.arrivalTime.text,
                                        style = TextStyle(
                                            color = MaterialTheme.colorScheme.onSecondary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    )
                                }
                                LazyRow(
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    for ((i, bigStep: Step) in leg.steps.withIndex()) {
                                        item {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                val travelModeText = getTravelModeText(bigStep)
                                                Row(
                                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                                    verticalAlignment = Alignment.Bottom
                                                ) {
                                                    Icon(
                                                        imageVector = getTravelModeIcon(
                                                            travelModeText
                                                        ),
                                                        travelModeText,
                                                        tint = MaterialTheme.colorScheme.onSecondary,
                                                    )
                                                    if (bigStep.transitDetails?.line != null) {
                                                        val text =
                                                            if (bigStep.transitDetails.line.shortName.isNotBlank()) {
                                                                bigStep.transitDetails.line.shortName
                                                            } else if (bigStep.transitDetails.line.name.isNotBlank()) {
                                                                bigStep.transitDetails.line.name
                                                            } else {
                                                                return@Row
                                                            }
                                                        val textColor =
                                                            if (bigStep.transitDetails.line.textColor.isNotBlank()) {
                                                                Color(bigStep.transitDetails.line.textColor.toColorInt())
                                                            } else {
                                                                MaterialTheme.colorScheme.onPrimaryContainer
                                                            }
                                                        val backgroundTextColor =
                                                            if (bigStep.transitDetails.line.color.isNotBlank()) {
                                                                Color(bigStep.transitDetails.line.color.toColorInt())
                                                            } else {
                                                                MaterialTheme.colorScheme.primaryContainer
                                                            }
                                                        Text(
                                                            text = text,
                                                            style = TextStyle(color = textColor),
                                                            modifier = Modifier
                                                                .clip(MaterialTheme.shapes.small)
                                                                .background(backgroundTextColor)
                                                                .padding(2.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = getTravelTime(bigStep),
                                                    style = TextStyle(color = MaterialTheme.colorScheme.onSecondary)
                                                )
                                            }
                                            if (i < leg.steps.lastIndex) {
                                                Icon(
                                                    imageVector = Icons.Rounded.ChevronRight,
                                                    ">",
                                                    tint = MaterialTheme.colorScheme.onSecondary,
                                                )
//                                        Spacer(Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "Error" -> ShowError(text = "Directions data not available.")
                "Empty" -> ShowError(text = "Empty response.")
                else -> {}
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
                val msg = "Permissons granted to send notifications";
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

fun getLocalTime(ts: Long?): String {
    if (ts == null) return ""
    //Get instance of calendar
    val calendar = Calendar.getInstance(Locale.getDefault())
    //get current date from ts
    calendar.timeInMillis = ts * 1000
    //return formatted date
    return DateFormat.format("HH:mm", calendar).toString()
}

fun getTravelModeText(bigStep: Step): String {
    return if (bigStep.travelMode == "TRANSIT") {
        bigStep.transitDetails?.line?.vehicle?.type ?: "TRANSIT"
    } else {
        bigStep.travelMode
    }
}

fun getTravelTime(bigStep: Step): String {
    return if (bigStep.travelMode == "TRANSIT") {
        (getLocalTime(bigStep.transitDetails?.departureTime?.value) + "-" + getLocalTime(bigStep.transitDetails?.arrivalTime?.value))
    } else {
        bigStep.duration.text
    }
}


private fun getTravelModeIcon(travelMode: String) = when (travelMode) {
    "TRANSIT" -> Icons.Rounded.DirectionsTransit
    "WALKING" -> Icons.AutoMirrored.Rounded.DirectionsWalk
    "BICYCLING" -> Icons.AutoMirrored.Rounded.DirectionsBike
    "DRIVING" -> Icons.Rounded.DirectionsCar
    "BUS" -> Icons.Rounded.DirectionsBus
    "TRAM" -> Icons.Rounded.Tram
    "HEAVY_RAIL" -> Icons.Rounded.DirectionsRailway
    "BOAT" -> Icons.Rounded.DirectionsBoat
    else -> Icons.Rounded.QuestionMark
}