package com.miniproject.cropharv

data class WeatherResponseModel(
    val weather: List<Weather>,
    val main: Main,
    val rain: Rain?,
    val snow: Snow?, // Added for snow data
    val clouds: Clouds?, // Added for cloudiness data
    val sys: Sys? // Added for sunrise and sunset data
)

data class Weather(
    val id: Int,
    val description: String,
    val icon: String
)

data class Main(
    val temp: Double,
    val humidity: Int,
    val feels_like: Double,
    val pressure: Int
)

data class Rain(
    val `1h`: Double? // Rain volume in the last 1 hour
)

data class Snow(
    val `1h`: Double? // Snow volume in the last 1 hour
)

data class Clouds(
    val all: Int? // Cloudiness percentage
)

data class Sys(
    val sunrise: Long?, // Sunrise time in UNIX timestamp
    val sunset: Long? // Sunset time in UNIX timestamp
)
