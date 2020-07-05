package com.fabio.weatherapp.view

import android.R.attr.animationDuration
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fabio.weatherapp.DeviceHelper.convertUTC_to_local_Timezone
import com.fabio.weatherapp.DeviceHelper.convertWeatherStateToDrawableName
import com.fabio.weatherapp.DeviceHelper.extractTime
import com.fabio.weatherapp.R
import com.fabio.weatherapp.adapter.ForecastAdapter
import com.fabio.weatherapp.databinding.FragmentDetailsBinding
import com.fabio.weatherapp.viewmodel.DetailsActivityViewModel
import kotlinx.android.synthetic.main.fragment_details.*
import kotlin.math.roundToInt


class DetailsFragment : Fragment() {

    private lateinit var viewModel: DetailsActivityViewModel
    private var woeid: Int? = null
    private var locationName: String? = null
    private lateinit var binding: FragmentDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //TODO DEBUG
        woeid = arguments?.getInt("WOEID")
//        woeid = 44418
        locationName = arguments?.getString("LOCATION_NAME")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_details, container, false)

        Log.d("fdl.DetailsFragment", "woeid:$woeid locationName:$locationName")
        binding.fragment = this
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(DetailsActivityViewModel::class.java)

        tv_locationName.text = locationName

        woeid?.let {
            viewModel.getWeather(it)
        }

        viewModel.showProgress.observe(viewLifecycleOwner, Observer {
            if (it) {
                mProgressBar.visibility = View.VISIBLE
            } else {
                mProgressBar.visibility = View.GONE
            }
        })


        // RecyclerView Forecast
        val mForecastAdapter = ForecastAdapter(this)
        forecastRV.adapter = mForecastAdapter
        forecastRV.isNestedScrollingEnabled = false
        val mLayoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        forecastRV.layoutManager = mLayoutManager
        val dividerItemDecoration = DividerItemDecoration(
            forecastRV.context,
            mLayoutManager.orientation
        )
        forecastRV.addItemDecoration(dividerItemDecoration)





        viewModel.response.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                Log.d("fdl", "consolidated_weather: ${it.consolidated_weather[0]}")

                // XXXXX
                // fdl [0] is today
                mForecastAdapter.weatherDataList = it.consolidated_weather
                mForecastAdapter.notifyDataSetChanged()

                Log.d("fdl.time", "it.sun_rise ${it.sun_rise}")
                Log.d("fdl.time", "it.sun_set ${it.sun_set}")
                text_sunrise.text = if (TextUtils.isEmpty(it.sun_rise)) " --- " else {
                    extractTime(it.sun_rise)
                }
                text_sunset.text = if (TextUtils.isEmpty(it.sun_set)) " --- " else {
                    extractTime(it.sun_set)
                }

                val mWeather = it.consolidated_weather[0]

                val roundedTemperature = mWeather.the_temp.roundToInt()
                val roundedAirPressure = mWeather.air_pressure.roundToInt()
                val roundedVisibility = mWeather.visibility.roundToInt()
                val roundedWindSpeed = mWeather.wind_speed.roundToInt()

                text_temperature.text =
                    resources.getString(R.string.temperature, roundedTemperature.toString())
                text_main_weather.text = it.consolidated_weather[0].weather_state_name
                val updatedTime = convertUTC_to_local_Timezone(it.consolidated_weather[0].created)
                text_last_update.text = resources.getString(R.string.last_updated, updatedTime)
                text_pressure.text =
                    resources.getString(R.string.air_pressure, roundedAirPressure.toString())
                text_humidity.text =
                    resources.getString(R.string.air_pressure, mWeather.humidity.toString())
                text_visibility.text =
                    resources.getString(R.string.visibility, roundedVisibility.toString())
                text_wind.text = resources.getString(
                    R.string.wind_speed,
                    roundedWindSpeed.toString(),
                    mWeather.wind_direction_compass
                )

                convertWeatherStateToDrawableName(mWeather.weather_state_abbr)?.let { id ->
                    image_icon.setImageResource(id)
                }


                // Wind Arrow
                val windDirection = mWeather.wind_direction
                val rotateAnimation = RotateAnimation(
                    windDirection.toFloat(),
                    0.0f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f
                )
                rotateAnimation.interpolator = DecelerateInterpolator()
                rotateAnimation.repeatCount = 0
                rotateAnimation.duration = animationDuration.toLong()
                rotateAnimation.fillAfter = true
                imageViewWindDirection.startAnimation(rotateAnimation)

            }
        })

    }

    fun navigateToSearchLocation() {
        parentFragment?.findNavController()?.navigate(R.id.detailsFragment_to_searchCityFragment)
    }


}
