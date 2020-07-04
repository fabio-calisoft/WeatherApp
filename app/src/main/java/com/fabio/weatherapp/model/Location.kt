package com.fabio.weatherapp.model

data class Location(
    var latt_long: String,
    var location_type: String,
    var title: String,
    var woeid: Int

)