package com.example.nexttransit.model.settings

import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object AppSettingsSerializer : Serializer<AppSettings> {
    override val defaultValue: AppSettings
        get() = AppSettings()

    override suspend fun readFrom(input: InputStream): AppSettings {
        return try {
            Json.Default.decodeFromString(
                deserializer = AppSettings.serializer(), string = input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            e.printStackTrace()
            defaultValue
        }
    }

    fun readFrom(input: String): AppSettings {
        return try {
            Json.Default.decodeFromString(
                deserializer = AppSettings.serializer(), string = input
            )
        } catch (e: SerializationException) {
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: AppSettings, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.Default.encodeToString(
                    serializer = AppSettings.serializer(),
                    value = t,
                ).encodeToByteArray()
            )
        }
    }
}