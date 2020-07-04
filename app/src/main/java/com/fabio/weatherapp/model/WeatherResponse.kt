package com.fabio.weatherapp.model

data class WeatherResponse(
    val consolidated_weather: List<ConsolidatedWeather>,
    val title: String,
    val location_type: String,
    val woeid: Int,
    val lattLong: String,
    val time: String
)
