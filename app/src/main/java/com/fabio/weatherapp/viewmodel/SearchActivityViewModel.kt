package com.fabio.weatherapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.fabio.weatherapp.model.Location
import com.fabio.weatherapp.repository.SearchActivityRespository

class SearchActivityViewModel(application: Application) :AndroidViewModel(application) {

    private val repository=SearchActivityRespository(application)
    val showProgress:LiveData<Boolean>

    val locationList:LiveData<List<Location>>

    init {
        this.showProgress=repository.showProgress
        this.locationList=repository.locationList
    }

    fun searchLocation(location:String){
       repository.searchLocation(location)
    }



}