package com.fabio.weatherapp.network


import com.fabio.weatherapp.model.ConsolidatedWeather
import com.fabio.weatherapp.model.Location
import com.fabio.weatherapp.model.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


const val BASE_URL = "https://www.metaweather.com/api/location/"

interface WeatherNetwork {

    @GET("search?")
    fun getLocationByName(@Query("query") location: String): Call<List<Location>>

    @GET("search?")
    fun getLocationByCoordinates(@Query("lattlong") lattlong: String): Call<List<Location>>

    @GET("{woeid}")
    fun getWeather(@Path("woeid") woeid: Int): Call<WeatherResponse>

    @GET("{woeid}/{year}/{month}/{day}")
    fun getWeatherForDate(
        @Path("woeid") woeid: Int,
        @Path("year") year: Int,
        @Path("month") month: Int,
        @Path("day") day: Int
    ): Call<List<ConsolidatedWeather>>
}