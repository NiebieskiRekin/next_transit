package com.example.nexttransit

import android.content.Context
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.glance.state.GlanceStateDefinition
import java.io.File

private const val DATA_STORE_FILENAME_PREFIX = "widgetSettings_"

class TransitWidgetStateDefinition : GlanceStateDefinition<AppSettings> {

    override suspend fun getDataStore(context: Context, fileKey: String) = DataStoreFactory.create(
        serializer = AppSettingsSerializer,
        produceFile = { getLocation(context, fileKey) }
    )

    override fun getLocation(context: Context, fileKey: String): File {
        return context.dataStoreFile(DATA_STORE_FILENAME_PREFIX + fileKey.lowercase())
    }

}