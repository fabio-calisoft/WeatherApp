package com.fabio.weatherapp.model

class WeatherResponse (
    val consolidated_weather: List<ConsolidatedWeather>,

    val title:String,
    val location_type: String,
    val woeid:Int,
    val lattLong:String,
    val time:String
)
