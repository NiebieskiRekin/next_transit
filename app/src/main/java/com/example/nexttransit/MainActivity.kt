package com.example.nexttransit

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color.parseColor
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsBike
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.core.content.ContextCompat.startActivity
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.example.nexttransit.ApiCaller.getSampleDirections
import com.example.nexttransit.ApiCaller.trimPolyline
import com.example.nexttransit.ui.theme.NextTransitTheme
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale


class MainActivity : ComponentActivity() {

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
                val appSettings = appSettingsDataStore.data.collectAsState(initial = AppSettings()).value
                val scope = rememberCoroutineScope()
                var source by remember { mutableStateOf(TextFieldValue(appSettings.source.name))}
                var destination by remember { mutableStateOf(TextFieldValue(appSettings.destination.name))}
                var directions by remember { mutableStateOf(appSettings.lastDirectionsResponse)}
                var prefsButtonText by remember { mutableStateOf("Update preferences")}

                // super weird
                val tertiary = MaterialTheme.colorScheme.tertiary
                val secondary = MaterialTheme.colorScheme.secondary
                val error = MaterialTheme.colorScheme.error
                var prefsButtonColor by remember { mutableStateOf(tertiary) }

                var directionsGenerated by remember { mutableStateOf(false)}

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        OutlinedTextField(
                            value = source,
                            onValueChange = {
                                source = it
                                prefsButtonColor = tertiary
                                prefsButtonText="Update preferences"
                                directionsGenerated = false},
                            label = { Text("Origin") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp, 0.dp)
                        )
                        OutlinedTextField(
                            value = destination,
                            onValueChange = {
                                destination = it
                                prefsButtonColor=tertiary
                                prefsButtonText="Update preferences"
                                directionsGenerated = false},
                            label = { Text("Destination") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp, 0.dp)
                        )

                        Row (horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()){
                            Button(onClick = {
                                scope.launch {
                                    try {
                                        directions = ApiCaller.getDirectionsByName(source.text,destination.text)
//                                        directions =  getSampleDirections()
                                        directionsGenerated = true
                                    } catch (e: Exception) {
                                        directions = DirectionsResponse(status = "Error")
                                        directionsGenerated = false
                                    }
                                }
                            }) {
                                Text("Show directions!")
                            }
                            Button(onClick = { scope.launch {
                                    try {
                                        updateSettings(source.text,destination.text,directions)
                                        prefsButtonText = "Updated"
                                        prefsButtonColor = secondary
//                                        onWidgetClicked(
//                                            source.text,destination.text,
//                                            directions.toString())

                                    } catch (e: Exception){
                                        prefsButtonText = "Error!"
                                        prefsButtonColor = error
                                    }
                                }},
                                colors = ButtonDefaults.buttonColors(prefsButtonColor),
                                enabled = source.text.isNotBlank() && destination.text.isNotBlank() && directionsGenerated

                            ){
                                Text(prefsButtonText)
                            }
                        }
                        Log.e("ApiResponse", directions.toString())
                        SimpleDisplay(directions,source.text,destination.text)

                        Spacer(
                            Modifier
                                .size(0.dp, 10.dp)
                                .fillMaxWidth())
                        var showDebugOutput by remember { mutableStateOf(false) }
                        TextButton(onClick={showDebugOutput=!showDebugOutput}, content={
                            Text(if (showDebugOutput) {
                                "Hide Debug Output"
                            } else {
                                "Show Debug Output"
                            })
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.Sharp.ChevronRight,
                                contentDescription = ">",
                                Modifier.rotate(if (showDebugOutput){ 90f} else {0f})
                            )
                                                 },
                            modifier=Modifier.padding(5.dp,0.dp))
                        if (showDebugOutput) {
                            LazyColumn(
                                modifier = Modifier
                                    .padding(5.dp)
                                    .border(2.dp, Color.LightGray)
                                    .padding(5.dp)
                            ) {
                                item {
                                    Text(
                                        text = "Saved origin:",
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
                                        text = "Saved destination:",
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
                                        text = "Saved last directions response:",
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
                                        text = "New Directions:",
                                        style = TextStyle(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        )
                                    )
                                    SelectionContainer {
                                        Text(text = trimPolyline(directions).toString())
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

//    private fun onWidgetClicked(source: String, destination: String, directions: String){
//        resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
//        resultValue.putExtra("source",source)
//        resultValue.putExtra("destination",destination)
//        resultValue.putExtra("directions",directions)
//        actionRunCallback<RefreshAction>()
//        setResult(Activity.RESULT_OK, resultValue)
//        finish()
//    }


    private suspend fun updateSettings(sourceName: String, destinationName: String, directions: DirectionsResponse){
        appSettingsDataStore.updateData {
            it.copy(
                source = Location(sourceName,directions.geocodedWaypoints[0].placeId),
                destination = Location(destinationName,directions.geocodedWaypoints[1].placeId),
                lastDirectionsResponse = directions
            )
        }
    }


    @Preview(showBackground=true)
    @Composable
    private fun SimpleDisplay(
        directions: DirectionsResponse = getSampleDirections(),
        source: String = "Poznań",
        destination: String = "Kraków"){
        NextTransitTheme {
            when (directions.status){
                "OK" -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.large)
                            .background(MaterialTheme.colorScheme.secondary)
                            .padding(10.dp)
                            .clickable(true, "Open Google Maps", null, onClick =
                            {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(
                                        "https://www.google.com/maps/dir/?api=1" +
                                                "&origin=${source}" +
                                                "&destination=${destination}" +
                                                "&travelmode=transit"
                                    )
                                )
                                startActivity(this@MainActivity, intent, null)
                            }),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (directions.routes.isEmpty()) {
                            item {
                                Text(
                                    text = "Error: no route found.",
                                    color = MaterialTheme.colorScheme.onSecondary,
                                )
                            }
                        }
                        items(directions.routes) { route: Route ->
                            for (leg: Leg in route.legs) {
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
                                                Row (horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom){
                                                    Icon(
                                                        imageVector = getTravelModeIcon(travelModeText),
                                                        travelModeText,
                                                        tint = MaterialTheme.colorScheme.onSecondary,
                                                    )
                                                    if (bigStep.transitDetails?.line != null) {
                                                        val text = if (bigStep.transitDetails.line.shortName.isNotBlank()){
                                                            bigStep.transitDetails.line.shortName
                                                        } else if (bigStep.transitDetails.line.name.isNotBlank()){
                                                            bigStep.transitDetails.line.name
                                                        } else {
                                                            return@Row
                                                        }
                                                        val textColor = if (bigStep.transitDetails.line.textColor.isNotBlank()) {
                                                            Color(parseColor(bigStep.transitDetails.line.textColor))
                                                        } else {
                                                            MaterialTheme.colorScheme.onPrimaryContainer
                                                        }
                                                        val backgroundTextColor =if (bigStep.transitDetails.line.color.isNotBlank()) {
                                                            Color(parseColor(bigStep.transitDetails.line.color))
                                                        } else  {
                                                            MaterialTheme.colorScheme.primaryContainer
                                                        }
                                                        Text(
                                                            text=text,
                                                            style=TextStyle(color=textColor),
                                                            modifier= Modifier
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

                "Error" -> Text(text="Error: directions data not available.")
                "Empty" -> Text(text="")
                else -> {}
            }
        }
    }

}

fun getLocalTime(ts:Long?):String{
    if(ts == null) return ""
    //Get instance of calendar
    val calendar = Calendar.getInstance(Locale.getDefault())
    //get current date from ts
    calendar.timeInMillis = ts*1000
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