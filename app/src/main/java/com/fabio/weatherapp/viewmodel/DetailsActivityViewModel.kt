package com.fabio.weatherapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.fabio.weatherapp.model.ConsolidatedWeather
import com.fabio.weatherapp.model.WeatherResponse
import com.fabio.weatherapp.repository.DetailsActivityRepository

class DetailsActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DetailsActivityRepository(application)
    val showProgress: LiveData<Boolean>
    val response: LiveData<WeatherResponse>
    val hourlyResponse: LiveData<ConsolidatedWeather>

    init {
        this.showProgress = repository.showProgress
        this.response = repository.response
        this.hourlyResponse = repository.hourlyResponse

    }

    fun getWeather(woeid: Int) {
        repository.getWeather(woeid)
    }

    fun getWeatherForDate(woeid: Int, year: Int, month: Int, day: Int) {
        repository.getWeatherForDate(woeid, year, month, day)
    }
}