package com.example.nexttransit.ui.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.nexttransit.api.ApiCaller
import com.example.nexttransit.api.ApiCaller.getSampleDirections
import com.example.nexttransit.model.routes.DirectionsResponse
import kotlinx.coroutines.launch

@Composable
fun DirectionsTextFieldsSettings(
    sourceSeed: String = "Poznań",
    destinationSeed: String = "Kraków",
    directions: Pair<Boolean, DirectionsResponse> = Pair(false, getSampleDirections()),
    update: suspend (String, String, DirectionsResponse) -> Unit,
    onGetDirectionsButtonClicked: (getDirectionsButtonClicked: Boolean) -> Unit,
    onDirectionsGet: (directionsGenerated: Boolean, source: String, destination: String, directions: DirectionsResponse) -> Unit
) {
    var source by remember { mutableStateOf(TextFieldValue(sourceSeed)) }
    var destination by remember { mutableStateOf(TextFieldValue(destinationSeed)) }
    var prefsButtonText by remember { mutableStateOf("Update preferences") }
    val scope = rememberCoroutineScope()

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
                    val result = try {
                        Pair(true, ApiCaller.getDirectionsByName(source.text, destination.text))
                    } catch (_: Exception) {
                        Pair(false, DirectionsResponse(status = "Error"))
                    }
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