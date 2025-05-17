package com.example.nexttransit

import android.Manifest
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
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.example.nexttransit.api.ApiCaller
import com.example.nexttransit.model.AppScreen
import com.example.nexttransit.model.routes.DirectionsResponse
import com.example.nexttransit.model.routes.Location
import com.example.nexttransit.model.settings.AppSettings
import com.example.nexttransit.model.settings.AppSettingsSerializer
import com.example.nexttransit.ui.app.CHANNEL_ID
import com.example.nexttransit.ui.app.DebugOutput
import com.example.nexttransit.ui.app.DirectionsTextFieldsSettings
import com.example.nexttransit.ui.app.LoadingDirectionsWidget
import com.example.nexttransit.ui.app.MyCalendarView
import com.example.nexttransit.ui.theme.NextTransitTheme
import com.example.nexttransit.ui.widget.TransitWidget
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlin.random.Random


class MainActivity : ComponentActivity() {
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
                sendNotification(
                    "test1",
                    "test2",
                    ApiCaller.getSampleDirections()
                )
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

    fun getNotificationBuilder(place1: String, place2: String, directions: DirectionsResponse): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Trasa: ${place1} - ${place2}")
            .setContentText(directions.routes[0].legs[0].steps[0].htmlInstructions)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        return builder
    }

    fun sendNotification(place1: String, place2: String, directions: DirectionsResponse){
        val builder = getNotificationBuilder(place1,place2,directions)
        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("Notifications","Permission not granted")
                // TODO: Consider calling
                // ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                // public fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                //                                        grantResults: IntArray)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                requestPermissionLauncher.launch("android.permission.POST_NOTIFICATIONS")

                return@with
            }
            // notificationId is a unique int for each notification that you must define.
            notify(Random.nextInt(), builder.build())
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainContent() {
        // Store which screen should be visible, changeable on the bottom bar
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
                    MyCalendarView(contentResolver) { place1, place2, directions ->
                        sendNotification(
                            place1,
                            place2,
                            directions
                        )
                    }
                }
                AppScreen.WidgetSettings -> {
                    val appSettings = appSettingsDataStore.data.collectAsState(initial = AppSettings()).value
                    WidgetSettingsView(
                        appSettings = appSettings,
                    )
                }
            }
        }
    }

    suspend fun UpdateWidgetData(){
        val resultValue: Intent = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, extractAppWidgetId())
        setResult(RESULT_OK, resultValue)
        val manager = GlanceAppWidgetManager(applicationContext)
        val widget = TransitWidget()
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

    @Composable
    fun WidgetSettingsView(appSettings: AppSettings){
        var directions1 by remember { mutableStateOf(appSettings.lastDirectionsResponse) }
        var directions1ButtonClicked by remember { mutableStateOf(false) }
        var source1 by remember { mutableStateOf(appSettings.source.name) }
        var destination1 by remember { mutableStateOf(appSettings.destination.name) }
        var directions1Generated by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        Scaffold(
            floatingActionButton = {
                if (directions1Generated) {
                    FloatingActionButton(
                        onClick = {
                            scope.launch { UpdateWidgetData() }
                        },
                    ) {
                        Icon(
                            Icons.Default.Save, "Save and exit"
                        )
                    }
                }
            }
        ) {
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
                        newDirections = directions1,
                    )
                }
            }
        }
    }


    fun askNotificationPermission() {
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

