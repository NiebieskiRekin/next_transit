package com.example.nexttransit

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import com.example.nexttransit.ui.theme.NextTransitTheme
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TransitWidgetConfigurationActivity : ComponentActivity(){

    private val appWidgetId = intent?.extras?.getInt(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID
    ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

    private var resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

    private lateinit var widgetState: AppSettings

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setResult(RESULT_CANCELED,resultValue)

        Log.d("TransitWidget","Configuration - start")

        setResult(RESULT_CANCELED,resultValue)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val glanceId: GlanceId = GlanceAppWidgetManager(this).getGlanceIdBy(appWidgetId)

        Log.d("TransitWidget","Configuration - just before content")

        setContent {
            NextTransitTheme {
                Content(glanceId)
            }
        }

        Log.d("TransitWidget","Configuration - past set content")
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun saveAndFinish(appWidgetId: Int, source: String, destination: String) {
        Log.d("TransitWidget", "save and finish: $source, $destination")
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
//        resultValue.putExtra("AppSettings-source", source)
//        resultValue.putExtra("AppSettings-destination", destination)
        setResult(Activity.RESULT_OK, resultValue)

        GlobalScope.launch {
            Log.d("TransitWidget","Inside scope")
            val context: Context = this@TransitWidgetConfigurationActivity
            val glanceId: GlanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
            val directionsResponse = ApiCaller.getDirectionsByName(source, destination)
            updateAppWidgetState(context, TransitWidgetStateDefinition(),glanceId) {
                it.copy(
                    source = Location(
                        placeId = directionsResponse.geocodedWaypoints[0].placeId,
                        name = source
                    ),
                    destination = Location(
                        placeId = directionsResponse.geocodedWaypoints[1].placeId,
                        name = destination
                    ),
                    lastDirectionsResponse = directionsResponse
                )
            }
            TransitWidget().update(context = context, id = glanceId)
            Log.d("TransitWidget", "Gl scope run")
            Log.d("TransitWidget",widgetState.toString())
        }
        finish()
    }

    @OptIn(ExperimentalGlanceRemoteViewsApi::class)
    @Composable
    fun Content(glanceId: GlanceId) {
        LaunchedEffect(key1 = glanceId) {
            widgetState = getAppWidgetState(this@TransitWidgetConfigurationActivity, TransitWidgetStateDefinition(), glanceId)
        }
        Log.d("TransitWidget", "Inside Content")
        var source by remember { mutableStateOf(TextFieldValue(widgetState.source.name)) }
        var destination by remember { mutableStateOf(TextFieldValue(widgetState.destination.name)) }
//        val widgetstate by remember{ mutableStateOf( AppSettings(Location("Poznań"),Location("Środa Wlkp."),ApiCaller.getSampleDirections()))}
//        var grv: RemoteViewsCompositionResult by remember { mutableStateOf(RemoteViewsCompositionResult(
//            RemoteViews(packageName, R.layout.glance_default_loading_layout)
//        )) }
        ScreenScaffold(source.text, destination.text) {
//            Text("Preview", modifier = Modifier.padding(8.dp))
//            LaunchedEffect(key1 = widgetstate) {
//                grv = GlanceRemoteViews().compose(this@TransitWidgetConfigureActivity,
//                    DpSize(250.dp,60.dp),widgetstate,Bundle()){
//                    TransitWidget()
//                }
//                Log.d("TransitWidget","Launched effect grv run")
//            }
//            AndroidView(factory= { View.inflate(this@TransitWidgetConfigureActivity,grv.remoteViews.layoutId,null)},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(
                value = source,
                onValueChange = { source = it },
                label = { Text("Origin") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp, 0.dp)
            )
            OutlinedTextField(
                value = destination,
                onValueChange = { destination = it },
                label = { Text("Destination") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp, 0.dp)
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ScreenScaffold(
        source: String,
        destination: String,
        content: @Composable () -> Unit = {}
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Configure Widget") }, colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            bottomBar = {},
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    Log.d("TransitWidget","Save button clicked: $source, $destination")
                    saveAndFinish(appWidgetId,source,destination)
                }) {
                    Icon(Icons.Default.Save, contentDescription = "Save widget configuration")
                }
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                content()
            }
        }
    }
}