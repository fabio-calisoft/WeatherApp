package com.fabio.weatherapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.fabio.weatherapp.model.WeatherResponse
import com.fabio.weatherapp.repository.DetailsActivityRepository

class DetailsActivityViewModel(application: Application):AndroidViewModel(application) {

    private val repository = DetailsActivityRepository(application)
    val showProgress: LiveData<Boolean>
    val response:LiveData<WeatherResponse>
    init {
        this.showProgress=repository.showProgress
        this.response=repository.response
    }

    fun getWeather(woeid:Int){
        repository.getWeather(woeid)
    }
}