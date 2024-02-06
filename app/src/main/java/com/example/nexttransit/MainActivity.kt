package com.example.nexttransit

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexttransit.ui.theme.NextTransitTheme
import com.example.nexttransit.ApiCaller.getSampleDirections
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private val destination = "ChIJC0kwPxJbBEcRaulLN8Dqppc"
    private val origin  = "ChIJLcfSImn7BEcRa3MR7sqwJsw"

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
                        val scope = rememberCoroutineScope()
                        var text by remember { mutableStateOf("Loading") }
                        LaunchedEffect(true){
                            scope.launch {
                                text = try {
                                    getSampleDirections().toString()
                                } catch (e: Exception) {
                                    e.localizedMessage ?: "error"
                                }
                            }
                        }
                        Log.e("ApiResponse", text)
                        Text(text=text)
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                for (bigStep: BigStep in leg.steps) {
                                    Text(
                                        text = bigStep.travel_mode,
                                        style = TextStyle(color = Color.White)
                                    )
                                    Text(text = " > ", style = TextStyle(color = Color.White))
                                    Spacer(Modifier.size(16.dp))
//                                    for (step: Step in bigStep.steps) {
//                                        Text(
//                                            text = step.travel_mode,
//                                            style = TextStyle(color = Color.White)
//                                        )
//                                        Text(text = " > ", style = TextStyle(color = Color.White))
//                                        Spacer(Modifier.size(16.dp))
//                                    }
                                }
                            }
                            Icon(
                                Icons.Rounded.Home,
                                contentDescription = "Localized description"
                            )
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


//fun getTransitIcon(travelMode: String) = when (travelMode) {
//    "TRANSIT" -> Icons.
//    "WALKING" -> Icons
//    "BICYCLING" -> Icons
//    "DRIVING" ->
//    else -> TODO()
//}