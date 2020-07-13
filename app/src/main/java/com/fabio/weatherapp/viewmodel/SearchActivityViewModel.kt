package com.fabio.weatherapp.viewmodel

import android.app.Application
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.fabio.weatherapp.model.Location
import com.fabio.weatherapp.repository.SearchActivityRepository

class SearchActivityViewModel @ViewModelInject constructor(
    application: Application,
    private val repository: SearchActivityRepository
) : AndroidViewModel(application) {

    val showProgress: LiveData<Boolean>

    val locationList: LiveData<List<Location>>

    init {
        this.showProgress = repository.showProgress
        this.locationList = repository.locationList
    }

    fun searchLocationByName(location: String) {
        repository.searchLocationByName(location)
    }

    fun searchLocationByCoordinates(latt: Double, long: Double) {
        repository.searchLocationByCoordinates(latt, long)
    }


}