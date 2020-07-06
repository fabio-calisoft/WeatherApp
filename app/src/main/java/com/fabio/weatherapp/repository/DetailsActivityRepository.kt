package com.fabio.weatherapp.repository

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.fabio.weatherapp.model.ConsolidatedWeather
import com.fabio.weatherapp.model.WeatherResponse
import com.fabio.weatherapp.network.BASE_URL
import com.fabio.weatherapp.network.WeatherNetwork
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class DetailsActivityRepository(var application: Application) {


    val showProgress = MutableLiveData<Boolean>()

    val response = MutableLiveData<WeatherResponse>()
    val hourlyResponse = MutableLiveData<ConsolidatedWeather>()

    fun getWeather(woeid: Int) {
        showProgress.value = true
        val retrofit =
            Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
                .build()

        val service = retrofit.create(WeatherNetwork::class.java)

        val call = service.getWeather(woeid)
        call.enqueue(object : Callback<WeatherResponse> {
            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                showProgress.value = false
                Toast.makeText(application, "Error while getting data", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(
                call: Call<WeatherResponse>,
                res: Response<WeatherResponse>
            ) {
                showProgress.value = false
                Log.d("fdl.getWeather ", "Response:${Gson().toJson(res.body())}")
                response.value = res.body()
            }
        })

    } // getWeather


    fun getWeatherForDate(woeid: Int, year: Int, month: Int, day: Int) {
        showProgress.value = true
        val logging = HttpLoggingInterceptor()

        logging.level = HttpLoggingInterceptor.Level.BODY

        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(logging)


        val retrofit =
            Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build()

        val service = retrofit.create(WeatherNetwork::class.java)

        val call = service.getWeatherForDate(woeid, year, month, day)
        call.enqueue(object : Callback<List<ConsolidatedWeather>> {
            override fun onFailure(call: Call<List<ConsolidatedWeather>>, t: Throwable) {
                showProgress.value = false
                Toast.makeText(
                    application,
                    "Error while getting data for getWeatherForDate",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(
                call: Call<List<ConsolidatedWeather>>,
                res: Response<List<ConsolidatedWeather>>
            ) {
                showProgress.value = false
                Log.d("fdl.getWeatherForDate ", "Response:${Gson().toJson(res.body())}")
                res.body()?.let {
                    if (it.isNotEmpty()) {
                        hourlyResponse.value = res.body()?.get(0)
                    }
                }

            }
        })

    } // getWeather


}