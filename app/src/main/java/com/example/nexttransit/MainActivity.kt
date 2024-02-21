package com.example.nexttransit

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsBike
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.DirectionsBoat
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.DirectionsRailway
import androidx.compose.material.icons.rounded.DirectionsTransit
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material.icons.rounded.Tram
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.dataStore
import com.example.nexttransit.ui.theme.NextTransitTheme
import com.example.nexttransit.ApiCaller.getSampleDirections
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale


val Context.dataStore by dataStore("app-settings.json", AppSettingsSerializer)

class MainActivity : ComponentActivity() {

//    private val destination = "ChIJC0kwPxJbBEcRaulLN8Dqppc"
//    private val origin  = "ChIJLcfSImn7BEcRa3MR7sqwJsw"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NextTransitTheme {

                val appSettings = dataStore.data.collectAsState(initial = AppSettings()).value
                val scope = rememberCoroutineScope()

                var directions by remember {mutableStateOf(DirectionsResponse(status="Empty"))}
                var source by remember { mutableStateOf(TextFieldValue(appSettings.source.name)) }
                var destination by remember { mutableStateOf(TextFieldValue(appSettings.destination.name)) }

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        TextField(
                            value = source,
                            onValueChange = { source = it },
                            label = { Text("Origin") }
                        )
                        TextField(
                            value = destination,
                            onValueChange = { destination = it},
                            label = { Text("Destination") }
                        )
                        Button(onClick = {
                            scope.launch {
                                try {
                                    directions = ApiCaller.getDirectionsByName(source.text,destination.text)
//                                    directions = getSampleDirections()
                                    setSource(
                                        name = source.text,
                                        placeId = directions.geocoded_waypoints[0].place_id
                                    )
                                    setDestination(
                                        name = destination.text,
                                        placeId = directions.geocoded_waypoints[1].place_id
                                    )
                                } catch (e: Exception) {
                                    DirectionsResponse(status="Error")
                                }

                            }
                        }){
                            Text("Show directions!")
                        }
                        Log.e("ApiResponse", directions.toString())
                        SimpleDisplay(directions)
                        Text(text = directions.toString())
                    }
                }
            }
        }
    }

    private suspend fun setSource(name: String, placeId: PlaceId){
        dataStore.updateData {
            it.copy(
                source = Location(name,placeId,Calendar.getInstance(Locale.getDefault()).timeInMillis),
                knownLocations = it.knownLocations.mutate { mutable ->
                    mutable.add(it.source)
                }
            )
        }
    }

    private suspend fun setDestination(name: String, placeId: PlaceId){
        dataStore.updateData {
            it.copy(
                destination = Location(name,placeId,Calendar.getInstance(Locale.getDefault()).timeInMillis),
                knownLocations = it.knownLocations.mutate { mutable ->
                    mutable.add(it.destination)
                }
            )
        }
    }


}

@Composable
fun SimpleDisplay(directions: DirectionsResponse){
    NextTransitTheme {
        when (directions.status){
            "OK" -> {
                LazyColumn(
                    modifier = Modifier
                        .background(Color.DarkGray),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (directions.routes.isEmpty()) {
                        item {
                            Text(text = "Error: no route found.")
                        }
                    }
                    items(directions.routes) { route: Route ->
                        for (leg: Leg in route.legs) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Departure: ",
                                    style = TextStyle(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                )
                                Text(
                                    text = leg.departure_time.text,
                                    style = TextStyle(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )
                                Spacer(Modifier.size(16.dp))
                                Text(
                                    text = "Planned Arrival: ",
                                    style = TextStyle(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                )
                                Text(
                                    text = leg.arrival_time.text,
                                    style = TextStyle(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )
                            }
                            LazyRow(verticalAlignment = Alignment.CenterVertically) {
                                for ((i, bigStep: BigStep) in leg.steps.withIndex()) {
                                    item {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            val travelModeText = getTravelModeText(bigStep)
//                                        Text(
//                                            text = travelModeText,
//                                            style = TextStyle(color = Color.White)
//                                        )
                                            Icon(
                                                imageVector = getTravelModeIcon(travelModeText),
                                                travelModeText
                                            )
                                            Text(
                                                text = getTravelTime(bigStep),
                                                style = TextStyle(color = Color.White)
                                            )
                                        }
                                        if (i < leg.steps.lastIndex) {
                                            Text(
                                                text = " > ",
                                                style = TextStyle(color = Color.White)
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

fun getShortDate(ts:Long?):String{
    if(ts == null) return ""
    //Get instance of calendar
    val calendar = Calendar.getInstance(Locale.getDefault())
    //get current date from ts
    calendar.timeInMillis = ts
    //return formatted date
    return android.text.format.DateFormat.format("E, dd MMM yyyy", calendar).toString()
}

fun getLocalTime(ts:Long?):String{
    if(ts == null) return ""
    //Get instance of calendar
    val calendar = Calendar.getInstance(Locale.getDefault())
    //get current date from ts
    calendar.timeInMillis = ts
    //return formatted date
    return android.text.format.DateFormat.format("E", calendar).toString()
}

fun getTravelModeText(bigStep: BigStep): String {
    return if (bigStep.travel_mode == "TRANSIT") {
        bigStep.transit_details?.line?.vehicle?.type ?: "TRANSIT"
    } else {
        bigStep.travel_mode
    }
}

fun getTravelTime(bigStep: BigStep): String {
    return if (bigStep.travel_mode == "TRANSIT") {
        (bigStep.transit_details?.departure_time?.text + "-" + bigStep.transit_details?.arrival_time?.text)
    } else {
        bigStep.duration.text
    }
}


fun getTravelModeIcon(travelMode: String) = when (travelMode) {
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