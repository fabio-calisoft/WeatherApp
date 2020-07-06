package com.fabio.weatherapp.repository

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.fabio.weatherapp.model.Location
import com.fabio.weatherapp.network.BASE_URL
import com.fabio.weatherapp.network.WeatherNetwork
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class SearchActivityRepository(var application: Application) {

    val showProgress = MutableLiveData<Boolean>()
    val locationList = MutableLiveData<List<Location>>()

    fun searchLocationByName(location: String) {
        showProgress.value = true
        //network Call

        val retrofit =
            Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
                .build()

        val service = retrofit.create(WeatherNetwork::class.java)

        val call = service.getLocationByName(location)
        call.enqueue(object : Callback<List<Location>> {
            override fun onFailure(call: Call<List<Location>>, t: Throwable) {
                showProgress.value = false
                Toast.makeText(application, "Error while getting dsata", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(
                call: Call<List<Location>>,
                response: Response<List<Location>>
            ) {
                Log.d("SearchRespository ", "Response${Gson().toJson(response.body())}")
                showProgress.value = false
                locationList.value = response.body()

            }
        })

    }


    fun searchLocationByCoordinates(latt: Double, long: Double) {
        showProgress.value = true
        //network Call

        val retrofit =
            Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
                .build()

        val service = retrofit.create(WeatherNetwork::class.java)

        val call = service.getLocationByCoordinates("$latt,$long")
        call.enqueue(object : Callback<List<Location>> {
            override fun onFailure(call: Call<List<Location>>, t: Throwable) {
                showProgress.value = false
                Toast.makeText(application, "Error while getting dsata", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(
                call: Call<List<Location>>,
                response: Response<List<Location>>
            ) {
                Log.d("SearchRespository ", "Response${Gson().toJson(response.body())}")
                showProgress.value = false
                locationList.value = response.body()

            }
        })

    }

}



