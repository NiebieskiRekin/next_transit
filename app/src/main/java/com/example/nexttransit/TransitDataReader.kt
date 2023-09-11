package com.example.nexttransit
import android.util.Log
import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class Book(
    val title: String,
    val rating: Float
)

@Serializable
data class User(
    val name: String,
    val age: Int,
    val favouriteFruits: List<String>,
    val booksRead: List<Book>
)

object TransitDataReader {
    fun read(){
        val data = Json.decodeFromString<List<User>>("""
        [
  {
    "name": "Tomek",
    "age": 20,
    "favouriteFruits": [
      "apple",
      "banana",
      "orange"
    ],
    "booksRead": [
      {
        "title": "Arduino Guide",
        "rating": 4.0
      },
      {
        "title": "Thinking Fast and Slow",
        "rating": 4.1
      },
      {
        "title": "Dziady cz. III",
        "rating": 1.0
      }
    ]
  },
  {
    "name": "Hania",
    "age": 18,
    "favouriteFruits": [
      "strawberry",
      "tangerine",
      "blueberry"
    ],
    "booksRead": [
      {
        "title": "American Psycho",
        "rating": 3.9
      },
      {
        "title": "If we were villains",
        "rating": 4.3
      },
      {
        "title": "Historia z Operonem",
        "rating": 1.3
      }
    ]
  }
]
    """)
        Log.d("CREATION", data.toString())
    }

    fun mapRead(){
        // TODO: read from https request
        // TODO: parse JSON
        // TODO: display route data
    }
}