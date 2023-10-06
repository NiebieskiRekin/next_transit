package com.example.nexttransit


import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.*
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object ApiCaller {
    private val client = HttpClient(Android) {
        install(Logging)
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            }
            )
        }
    }


    suspend fun getDirections( destination: String, origin: String): DirectionsResponse {
        val apiKey = ""
        val response: DirectionsResponse = client.get {
            url {
                protocol = URLProtocol.HTTPS
                host = "maps.googleapis.com"
                path("/maps/api/directions/json")
                parameters.append("destination","place_id:$destination")
                parameters.append("origin","place_id:$origin")
                parameters.append("mode","transit")
                parameters.append("language","pl")
                parameters.append("key", apiKey)
            }
        }.body()
        return response
    }
}