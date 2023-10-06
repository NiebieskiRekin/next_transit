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
import com.example.nexttransit.ApiCaller.getDirections
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
                                    getDirections(destination,origin).toString()
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Row(verticalAlignment = Alignment.CenterVertically){
                Text(text="A", style= TextStyle(color = Color.White))
                Text(text=" > ", style= TextStyle(color = Color.White))
                Text(text="B", style= TextStyle(color = Color.White))
                Spacer(Modifier.size(16.dp))
                Text(text="16 min")
            }
            Text(
                text="Departure: 21:37",
                style= TextStyle(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize=14.sp
                ))
        }
    }
}