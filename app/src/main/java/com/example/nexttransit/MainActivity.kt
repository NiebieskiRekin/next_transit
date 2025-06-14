package com.example.nexttransit

import android.Manifest
import android.app.Activity.RESULT_FIRST_USER
import android.appwidget.AppWidgetManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import coil3.compose.AsyncImage
import com.example.nexttransit.api.NextTransitWorker
import com.example.nexttransit.model.AppScreen
import com.example.nexttransit.model.calendar.TZ
import com.example.nexttransit.model.database.DirectionsDatabase
import com.example.nexttransit.model.database.DirectionsDatabaseModule
import com.example.nexttransit.model.database.DirectionsQueryViewModel
import com.example.nexttransit.model.database.DirectionsState
import com.example.nexttransit.model.database.classes.DirectionsQuery
import com.example.nexttransit.model.routes.DirectionsResponse
import com.example.nexttransit.model.routes.Location
import com.example.nexttransit.model.settings.AppSettings
import com.example.nexttransit.model.settings.AppSettingsSerializer
import com.example.nexttransit.ui.app.CHANNEL_ID
import com.example.nexttransit.ui.app.ColumnPill
import com.example.nexttransit.ui.app.DebugOutput
import com.example.nexttransit.ui.app.DirectionsTextFieldsSettings
import com.example.nexttransit.ui.app.DirectionsWidget
import com.example.nexttransit.ui.app.DoubleEvent
import com.example.nexttransit.ui.app.LoadingDirectionsWidget
import com.example.nexttransit.ui.app.MyCalendarView
import com.example.nexttransit.ui.app.SwipeableListItem
import com.example.nexttransit.ui.theme.NextTransitTheme
import com.example.nexttransit.ui.widget.TransitWidget
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.random.Random
import androidx.compose.ui.text.TextStyle
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var db: DirectionsDatabase

    private val firestoreDb = Firebase.firestore

    @Suppress("UNCHECKED_CAST")
    private val viewModel by viewModels<DirectionsQueryViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DirectionsQueryViewModel(db.directionsQueryDao) as T
                }
            }
        }
    )

    private fun startSignIn() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.EmailBuilder().build()
        )

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.ic_launcher)
            .setLockOrientation(true)
            .setTheme(R.style.Theme_NextTransit_LoginTheme)
            .build()
        signInLauncher.launch(signInIntent)
    }

    private lateinit var auth: FirebaseAuth

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        Log.d("FirebaseAuth", result.resultCode.toString());
        when (result.resultCode) {
            RESULT_OK -> {
                Toast.makeText(baseContext, "Zalogowano", Toast.LENGTH_SHORT).show()
                val user = auth.currentUser
                Log.d("FirebaseAuth", user.toString());
            }

            RESULT_CANCELED -> {
                Toast.makeText(baseContext, "Zalogowano przez google", Toast.LENGTH_SHORT).show()
            }

            RESULT_FIRST_USER -> {
                val user = auth.currentUser
                Log.d("FirebaseAuth", user.toString());
            }

            else -> {
                Toast.makeText(baseContext, "Nieznany błąd logowania", Toast.LENGTH_SHORT).show()
            }
        }
        Log.d("FirebaseAuth", response.toString())
    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }

    private val requestNotificationPermissionLauncher = registerForActivityResult(
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
        auth = Firebase.auth
        setResultBasedOnWidgetId(appWidgetId, resultValue)


        val workRequest = OneTimeWorkRequestBuilder<NextTransitWorker>()
            .setInitialDelay(java.time.Duration.ofSeconds(15))
            .setBackoffCriteria(
                backoffPolicy = BackoffPolicy.LINEAR,
                duration = java.time.Duration.ofSeconds(15)
            ).build()

        WorkManager.getInstance(applicationContext).enqueue(workRequest)

        setContent {
            NextTransitTheme {
                MainContent()
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser == null) {
            startSignIn()
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

    fun getNotificationBuilder(
        place1: String,
        place2: String,
        directions: DirectionsResponse
    ): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Trasa: ${place1} - ${place2}")
            .setContentText(directions.routes[0].legs[0].steps[0].htmlInstructions)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        return builder
    }

    fun sendNotification(place1: String, place2: String, directions: DirectionsResponse) {
        val builder = getNotificationBuilder(place1, place2, directions)
        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("Notifications", "Permission not granted")
                // TODO: Consider calling
                // ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                // public fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                //                                        grantResults: IntArray)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                requestNotificationPermissionLauncher.launch("android.permission.POST_NOTIFICATIONS")

                return@with
            }
            // notificationId is a unique int for each notification that you must define.
            notify(Random.nextInt(), builder.build())
        }
    }


    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    fun StartView(stateFlow: StateFlow<DirectionsState>) {
        val state = stateFlow.collectAsState()
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Image(
                                painterResource(R.drawable.ic_launcher_foreground),
                                "Next transit logo",
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clip(
                                        CircleShape
                                    )
                            )
                            Text("Next Transit")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                )
            }
        ) { padding ->
            LazyColumn(
                Modifier.padding(padding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        "Zapisane trasy:",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(4.dp)
                    )
                }

                itemsIndexed(state.value.directions) { i, v ->
                    if (i == 0) {
                        Spacer(
                            Modifier
                                .padding(8.dp)
                                .fillMaxWidth()
                        )

                        Text(
                            "${v.firstEvent.startDateTime.toLocalDateTime(TZ).date}",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    } else {
                        val isSameDate =
                            v.firstEvent.startDateTime.toLocalDateTime(TZ).date != state.value.directions[i - 1].firstEvent.startDateTime.toLocalDateTime(
                                TZ
                            ).date
                        if (isSameDate) {
                            Spacer(
                                Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth()
                            )
                            Text("${v.firstEvent.startDateTime.toLocalDateTime(TZ).date}")
                        }
                    }

                    SwipeableListItem(
                        content = {
                            DoubleEvent(v.firstEvent, v.secondEvent)
                            if (v.firstEvent.place == v.secondEvent.place) {
                                val context = LocalContext.current
                                val encodedLocation = Uri.encode(v.firstEvent.place)
                                val mapsUrl =
                                    "https://www.google.com/maps/search/?api=1&query=$encodedLocation"

                                ColumnPill(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .clickable {
                                            val intent =
                                                Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl))
                                            context.startActivity(intent)
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Place,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSecondary,
                                            modifier = Modifier
                                                .padding(end = 12.dp)
                                                .size(32.dp)
                                        )

                                        Column(
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Wydarzenia odbywają się w tym samym miejscu:",
                                                style = TextStyle(
                                                    color = MaterialTheme.colorScheme.onSecondary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            )
                                            Text(
                                                text = v.firstEvent.place,
                                                style = TextStyle(
                                                    color = MaterialTheme.colorScheme.onSecondary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Normalny widok widgetu trasy
                                DirectionsWidget(
                                    directions = v.directionsResponse,
                                    source = v.firstEvent.place,
                                    destination = v.secondEvent.place
                                )
                            }
                        },
                        onDelete = {
                            scope.launch {
                                Log.d("DirectionsQueryViewModel", "Deleting $v")
                                db.directionsQueryDao.deleteDirectionsQuery(v)

//                                dropFirebase(firestoreDb, "next-transit", auth.currentUser?.uid!!)
//                                saveListOfObjectsToSubcollectionBatch(
//                                    firestoreDb,
//                                    "next-transit",
//                                    auth.currentUser?.uid!!,
//                                    state.value.directions
//                                )
                                Toast.makeText(
                                    applicationContext,
                                    "Deleted directions",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        addToCalendar = {
                            if (v.firstEvent.place == v.secondEvent.place) return@SwipeableListItem

                            Log.d("CALENDAR", v.firstEvent.calendarId.toString())

                            val departure = v.directionsResponse.routes.first().legs.first().departureTime.value
                            val arrival = v.directionsResponse.routes.first().legs.last().arrivalTime.value

                            // Jeśli API podało sekundy, przeliczamy na milisekundy
                            val departureMillis = if (departure < 1000000000000L) departure * 1000 else departure
                            val arrivalMillis = if (arrival < 1000000000000L) arrival * 1000 else arrival

                            val intent = Intent(Intent.ACTION_INSERT).apply {
                                data = CalendarContract.Events.CONTENT_URI
                                putExtra(CalendarContract.Events.CALENDAR_ID, v.firstEvent.calendarId)
                                putExtra(CalendarContract.Events.TITLE, "Podróż do ${v.secondEvent.place}")
                                putExtra(
                                    CalendarContract.Events.DESCRIPTION,
                                    "Podróż z ${v.firstEvent.place} do ${v.secondEvent.place}.\n" +
                                            HtmlCompat.fromHtml(
                                                v.directionsResponse.routes.first().legs.first().steps.first().htmlInstructions,
                                                HtmlCompat.FROM_HTML_MODE_LEGACY
                                            )
                                )
                                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, departureMillis)
                                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, arrivalMillis)
                            }

                            context.startActivity(intent)
                        }
                    )
                }
            }
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
                        icon = {
                            Icon(
                                it.icon,
                                contentDescription = stringResource(it.contentDescription)
                            )
                        },
                        label = { Text(stringResource(it.title)) },
                        selected = currentDestination == it,
                        onClick = { currentDestination = it }
                    )
                }
            },
            Modifier.fillMaxSize()
        ) {
            when (currentDestination) {
                AppScreen.Notifications -> {
                    NotificationsView()
                }

                AppScreen.Start -> {
                    val state = viewModel.state
                    StartView(state)
                }

                AppScreen.Calendar -> {
                    MyCalendarView(contentResolver) { event1, event2, directions, departAtOrArriveBy ->
                        db.directionsQueryDao.upsertDirectionsQuery(
                            DirectionsQuery(event1, event2, departAtOrArriveBy, directions)
                        )
                        currentDestination = AppScreen.Start
                    }
                }

                AppScreen.WidgetSettings -> {
                    val appSettings =
                        appSettingsDataStore.data.collectAsState(initial = AppSettings()).value
                    WidgetSettingsView(
                        appSettings = appSettings,
                    )
                }
            }
        }
    }


    suspend fun saveObjectToSubcollection(
        db: FirebaseFirestore,
        parentDocumentId: String,
        subcollectionName: String,
        obj: DirectionsQuery,
        objectId: String? = null
    ): Boolean {
        try {
            val docRef = if (objectId != null) {
                db.collection(parentDocumentId).document(parentDocumentId)
                    .collection(subcollectionName).document(objectId)
            } else {
                db.collection(parentDocumentId).document(parentDocumentId)
                    .collection(subcollectionName).document()
            }
            docRef.set(mapOf("data" to Json.encodeToString(obj))).await()
            Log.d("Firestore", "Object saved successfully to subcollection with ID: ${docRef.id}")
            return true
        } catch (e: Exception) {
            Log.e("Firestore", "Error saving object to subcollection", e)
            return false
        }
    }

    suspend fun dropFirebase(
        fdb: FirebaseFirestore,
        parentDocumentId: String,
        collectionId: String
    ) {
        fdb.collection(parentDocumentId).document(parentDocumentId)
            .collection(collectionId).document().delete().await()
    }

    suspend fun saveListOfObjectsToSubcollectionBatch(
        db: FirebaseFirestore,
        parentDocumentId: String,
        subcollectionName: String,
        objects: List<DirectionsQuery>
    ): Boolean {
        val batch = db.batch()
        objects.forEach { obj ->
            val docRef = db.collection(parentDocumentId).document(parentDocumentId)
                .collection(subcollectionName).document() // Auto-generate ID for each
            batch.set(docRef, mapOf("data" to Json.encodeToString(obj)))
        }
        try {
            batch.commit().await()
            Log.d(
                "Firestore",
                "${objects.size} objects saved successfully to subcollection in a batch."
            )
            return true
        } catch (e: Exception) {
            Log.e("Firestore", "Error saving objects to subcollection in a batch", e)
            return false
        }
    }


    suspend fun fetchObjectsFromSubcollection(
        db: FirebaseFirestore,
        parentDocumentId: String,
        subcollectionName: String
    ): List<DirectionsQuery> {
        val items = mutableListOf<DirectionsQuery>()
        try {
            val querySnapshot = db.collection(parentDocumentId).document(parentDocumentId)
                .collection(subcollectionName)
                .get()
                .await()
            for (document in querySnapshot.documents) {
                val data = document.get("data", String::class.java)
                if (data == null) continue;
                val dec = Json.decodeFromString<DirectionsQuery>(data)
                items.add(dec)
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching objects from subcollection", e)
        }
        return items
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun NotificationsView() {
        val scope = rememberCoroutineScope()
        if (auth.currentUser == null) {
            startSignIn()
        } else {
            val user = auth.currentUser!!
            Scaffold(
                topBar = {
                    TopAppBar(
                        actions = {
                            IconButton({ auth.signOut(); startSignIn() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Logout,
                                    "Logout"
                                )
                            }
                            AsyncImage(
                                user.photoUrl,
                                user.displayName ?: "User",
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clip(
                                        CircleShape
                                    )
                            )
                        },
                        title = { Text(auth.currentUser?.email.toString()) },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    )
                }
            ) { padding ->
                Column(modifier = Modifier.padding(padding)) {
                    Button({
                        scope.launch {
                            db.directionsQueryDao.getAllDirectionsQueries().collect { mydata ->
                                val res = saveListOfObjectsToSubcollectionBatch(
                                    firestoreDb,
                                    "next-transit",
                                    user.uid.toString(),
                                    mydata
                                )
                                if (res) {
                                    Toast.makeText(baseContext, "Saved!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(baseContext, "Error!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                    }) {
                        Text("Save to Firestore")
                    }
                    Button({
                        scope.launch {
                            val mydata = fetchObjectsFromSubcollection(
                                firestoreDb,
                                "next-transit",
                                user.uid.toString()
                            )
                            if (mydata.isNotEmpty()) {
                                try {
                                    db.directionsQueryDao.upsertAllDirectionsQuery(mydata)
                                    Toast.makeText(
                                        baseContext,
                                        "Sync complete!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } catch (e: Exception) {
                                    Toast.makeText(baseContext, "Error!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }) {
                        Text("Download from Firestore")
                    }
                }
            }
        }

    }

    suspend fun UpdateWidgetData() {
        val resultValue: Intent =
            Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, extractAppWidgetId())
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
    fun WidgetSettingsView(appSettings: AppSettings) {
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
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

}

