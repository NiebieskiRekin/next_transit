package com.example.nexttransit

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsBike
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.DirectionsBoat
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.DirectionsRailway
import androidx.compose.material.icons.rounded.DirectionsSubway
import androidx.compose.material.icons.rounded.DirectionsTransit
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material.icons.rounded.Tram
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexttransit.ui.theme.NextTransitTheme
import com.example.nexttransit.ApiCaller.getSampleDirections
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

//    private val destination = "ChIJC0kwPxJbBEcRaulLN8Dqppc"
//    private val origin  = "ChIJLcfSImn7BEcRa3MR7sqwJsw"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            NextTransitTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            SimpleDisplay()

                            val scope = rememberCoroutineScope()
                            var text by remember { mutableStateOf("Loading") }
                            var source by remember { mutableStateOf(TextFieldValue("")) }
                            var destination by remember { mutableStateOf(TextFieldValue("")) }
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
                                    text = try {
                                        ApiCaller.getDirectionsByName(source.text,destination.text).toString()
                                    } catch (e: Exception) {
                                        e.localizedMessage ?: "error"
                                    }
                                }
                            }){
                                Text("Show directions!")
                            }
                            Log.e("ApiResponse", text)
                            Text(text = text)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground=true)
@Composable
fun SimpleDisplay(){
    NextTransitTheme {
        val directions by remember { mutableStateOf(getSampleDirections()) }
        if (directions.status == "OK") {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (directions.routes.isNotEmpty()) {
                    for (route: Route in directions.routes) {
                        for (leg: Leg in route.legs) {
                            Text(
                                text = leg.departure_time.text,
                                style = TextStyle(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                for ((i, bigStep: BigStep) in leg.steps.withIndex()) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally){
                                        val travelModeText = getTravelModeText(bigStep)
                                        Text(
                                            text = travelModeText,
                                            style = TextStyle(color = Color.White)
                                        )
                                        Icon(imageVector=getTravelModeIcon(travelModeText),travelModeText)
                                    }
                                    if (i < leg.steps.lastIndex) {
                                        Text(text = " > ", style = TextStyle(color = Color.White))
//                                        Spacer(Modifier.size(16.dp))
                                    }
                                }
                            }
                            Text(
                                text = leg.arrival_time.text,
                                style = TextStyle(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
                } else {
                    Text(text="Error: no route found.")
                }
            }
        } else {
            Text(text="Error: directions data not available.")
        }
    }
}


fun getTravelModeText(bigStep: BigStep): String {
    return if (bigStep.travel_mode == "TRANSIT") {
        bigStep.transit_details?.line?.vehicle?.type ?: "TRANSIT"
    } else {
        bigStep.travel_mode
    }
}

//fun getTravelModeText(step: Step): String {
//    return if (step.travel_mode == "TRANSIT") {
//        step.transit_details?.line?.vehicle?.type ?: "TRANSIT"
//    } else {
//        step.travel_mode
//    }
//}

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