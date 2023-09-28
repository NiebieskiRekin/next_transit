package com.example.nexttransit

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.URLProtocol
import io.ktor.http.path

class Greeting {
    private val client = HttpClient()


    //https://maps.googleapis.com/maps/api/directions/json?destination=place_id:ChIJC0kwPxJbBEcRaulLN8Dqppc&origin=place_id:ChIJLcfSImn7BEcRa3MR7sqwJsw&mode=transit&language=pl&key=AIzaSyBSzgbK6yjBEkTjQjiQKOCVg_4lRm07sAs
    suspend fun greeting(): String {
        val destination = "ChIJC0kwPxJbBEcRaulLN8Dqppc"
        val origin  = "ChIJLcfSImn7BEcRa3MR7sqwJsw"
        val apiKey = ""
        val response = client.get {
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
        }
        return response.bodyAsText()
    }
}