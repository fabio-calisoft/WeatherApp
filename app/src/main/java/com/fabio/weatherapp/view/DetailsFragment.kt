package com.fabio.weatherapp.view

import android.R.attr.animationDuration
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fabio.weatherapp.DateHelper
import com.fabio.weatherapp.DateHelper.convertUTC_to_local_Timezone
import com.fabio.weatherapp.DateHelper.extractTime
import com.fabio.weatherapp.DeviceHelper
import com.fabio.weatherapp.DeviceHelper.checkLocationPermission
import com.fabio.weatherapp.DeviceHelper.convertWeatherStateToDrawableName
import com.fabio.weatherapp.DeviceHelper.loadDataLocationName
import com.fabio.weatherapp.DeviceHelper.loadDataWoeid
import com.fabio.weatherapp.DeviceHelper.saveData
import com.fabio.weatherapp.R
import com.fabio.weatherapp.adapter.ForecastAdapter
import com.fabio.weatherapp.databinding.FragmentDetailsBinding
import com.fabio.weatherapp.model.ConsolidatedWeather
import com.fabio.weatherapp.viewmodel.DetailsActivityViewModel
import com.fabio.weatherapp.viewmodel.SearchActivityViewModel
import kotlinx.android.synthetic.main.fragment_details.*
import kotlinx.android.synthetic.main.loading_progress.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


class DetailsFragment : Fragment(), LocationListener {

    private lateinit var viewModel: DetailsActivityViewModel
    private lateinit var viewModelNetwork: SearchActivityViewModel
    private var woeid: Int? = null
    // DEBUG private var woeid: Int? = 44418

    private lateinit var binding: FragmentDetailsBinding

    private var calendarYear: Int? = null
    private var calendarMonth: Int? = null
    private var calendarDay: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_details, container, false)
        binding.fragment = this
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("fdl.DetailsFragment", "onViewCreated")
        viewModel = ViewModelProvider(this).get(DetailsActivityViewModel::class.java)
        viewModelNetwork = ViewModelProvider(this).get(SearchActivityViewModel::class.java)

        viewModel.showProgress.observe(viewLifecycleOwner, Observer {
            manageProgressBar(it, "Loading Weather data")
        })

        viewModelNetwork.showProgress.observe(viewLifecycleOwner, Observer {
            manageProgressBar(it, "Loading Location data")
        })

        /*
         * Observer for List of Locations when the gps coordinates are used
         */
        viewModelNetwork.locationList.observe(viewLifecycleOwner, Observer {
            it?.let { aList ->
                woeid = aList[0].woeid
                tv_locationName.text = aList[0].title
                woeid?.let { mWoeid ->
                    saveData(activity, mWoeid, aList[0].title)

                    // get the weather for this Location
                    viewModel.getWeather(mWoeid)
                }
            }

        })

        // SwipeRefreshLayout
        swipe_container.setOnRefreshListener {

            // dispatches execution into Android main thread
            val uiScope = CoroutineScope(Dispatchers.Main)
            uiScope.launch {
                woeid?.let {
                    viewModel.getWeather(it)
                }
                swipe_container.isRefreshing = false
            }
        }


        arguments?.let {
            if (it.containsKey("WOEID")) {
                woeid = arguments?.getInt("WOEID")
                tv_locationName.text = arguments?.getString("LOCATION_NAME")
                saveData(activity, woeid, arguments?.getString("LOCATION_NAME"))
            }

            if (it.containsKey("YEAR") && it.containsKey("MONTH") && it.containsKey("DAY")) {
                calendarYear = arguments?.getInt("YEAR")
                calendarMonth = arguments?.getInt("MONTH")
                calendarDay = arguments?.getInt("DAY")
            }

        }

        // If I don't have a location (woeid) yet, let's check in SharedPref.
        // If empty, let's query Location Manager
        if (woeid == null) {
            // do I have the location in Shared Preferences ?
            woeid = loadDataWoeid(activity)
            if (woeid != null) { // ok, I have woeid from SP
                tv_locationName.text = loadDataLocationName(activity)
            } else {
                // I don't have woeid. let's use Location Manager
                readLocation()
            }

        }

        woeid?.let {
            // Check if it's coming from the calendar POST
            if (calendarYear != null && calendarMonth != null && calendarDay != null &&
                calendarYear!! > 0 && calendarMonth!! > 0 && calendarDay!! > 0
            ) {
                // Get Weather some date in the past
                viewModel.getWeatherForDate(
                    it,
                    calendarYear!!,
                    calendarMonth!!,
                    calendarDay!!
                )
                calendarYear = null
                calendarMonth = null
                calendarDay = null
            } else {
                // Get Weather today
                viewModel.getWeather(it)
            }
        }


        // RecyclerView Forecast
        val mForecastAdapter = ForecastAdapter()
        forecastRV.adapter = mForecastAdapter
        forecastRV.isNestedScrollingEnabled = false
        val mLayoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        forecastRV.layoutManager = mLayoutManager
        val dividerItemDecoration = DividerItemDecoration(
            forecastRV.context,
            mLayoutManager.orientation
        )
        forecastRV.addItemDecoration(dividerItemDecoration)

        viewModel.hourlyResponse.observe(viewLifecycleOwner, Observer {
            // received data for a day in the past
            if (it != null) {
                Log.d("fdl", "single ConsolidatedWeather: $it")
                updateTodayWeatherUI(it)

                // remove forecast
                val today = DateHelper.getDashFormattedTodayDate()
                if (it.applicable_date != today) {
                    forecastRV.visibility = View.INVISIBLE
                    text_forecast.visibility = View.INVISIBLE
                } else {
                    forecastRV.visibility = View.VISIBLE
                    text_forecast.visibility = View.VISIBLE
                }
            }


        })



        viewModel.response.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                Log.d("fdl", "consolidated_weather: ${it.consolidated_weather[0]}")

                tv_locationName.text = it.title

                // Forecast +++ fdl [0] is today
                forecastRV.visibility = View.VISIBLE
                if (it.consolidated_weather.size > 1) {
                    text_forecast.visibility = View.VISIBLE
                    mForecastAdapter.weatherDataList =
                        it.consolidated_weather.subList(1, it.consolidated_weather.size)
                    mForecastAdapter.notifyDataSetChanged()
                } else {
                    text_forecast.visibility = View.INVISIBLE
                }
                // Forecast --- fdl [0] is today

                Log.d("fdl.time", "it.sun_rise ${it.sun_rise}")
                Log.d("fdl.time", "it.sun_set ${it.sun_set}")
                text_sunrise.text = if (TextUtils.isEmpty(it.sun_rise)) " --- " else {
                    extractTime(it.sun_rise)
                }
                text_sunset.text = if (TextUtils.isEmpty(it.sun_set)) " --- " else {
                    extractTime(it.sun_set)
                }


                updateTodayWeatherUI(it.consolidated_weather[0])
            }
        })

    }

    /**
     * Updated the UI part of today's weather (no forecast)
     */
    private fun updateTodayWeatherUI(weather: ConsolidatedWeather) {
        val roundedTemperature = weather.the_temp.roundToInt()
        val roundedAirPressure = weather.air_pressure.roundToInt()
        val roundedVisibility = weather.visibility.roundToInt()
        val roundedWindSpeed = weather.wind_speed.roundToInt()

        text_temperature.text =
            resources.getString(R.string.temperature, roundedTemperature.toString())
        text_main_weather.text = weather.weather_state_name
        val updatedTime = convertUTC_to_local_Timezone(weather.created)
        text_last_update.text = resources.getString(R.string.last_updated, updatedTime)
        text_pressure.text =
            resources.getString(R.string.air_pressure, roundedAirPressure.toString())
        text_humidity.text =
            resources.getString(R.string.air_pressure, weather.humidity.toString())
        text_visibility.text =
            resources.getString(R.string.visibility, roundedVisibility.toString())
        text_wind.text = resources.getString(
            R.string.wind_speed,
            roundedWindSpeed.toString(),
            weather.wind_direction_compass
        )

        convertWeatherStateToDrawableName(weather.weather_state_abbr)?.let { id ->
            image_icon.setImageResource(id)
        }


        // Wind Arrow
        val windDirection = weather.wind_direction
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


    // =====  ++++ GPS Location Manager
    override fun onLocationChanged(location: Location) {
        Log.d(
            "fdl", "onLocationChanged Lat: " + location.latitude + " Lng: "
                    + location.longitude
        )
        activity?.let {
            val locationManager =
                it.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
            locationManager.removeUpdates(this)
        }

        viewModelNetwork.searchLocationByCoordinates(
            location.latitude,
            location.longitude
        )
        manageProgressBar(false, "Reading gps location")
    }


    override fun onStatusChanged(
        provider: String,
        status: Int,
        extras: Bundle
    ) {
    }

    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    private fun readLocation() {
        Log.d("fdl", "readLocation")

        activity?.let {
            if (checkLocationPermission(it, this)) {
                Log.d("fdl", "readLocation granted")
                val locationManager =
                    it.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
                manageProgressBar(true, "Reading gps location")
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 5000, 10f, this
                )
            } else {
                Log.w("fdl", "readLocation NOT granted...")
            }
        }

    }
    // =====  ---- GPS Location Manager


    /**
     * Nav-graph
     */
    fun openCalendar() {
        parentFragment?.findNavController()
            ?.navigate(R.id.action_detailsFragment_to_calendarFragment)
    }

    fun navigateToSearchLocation() {
        parentFragment?.findNavController()?.navigate(R.id.detailsFragment_to_searchCityFragment)
    }


    /**
     * Show/Hide the progressbar with message
     */
    private fun manageProgressBar(isActive: Boolean, message: String) {
        if (isActive) {
            mProgressBar?.let {
                it.textViewMessage.text = message
                mProgressBar?.visibility = View.VISIBLE
            }
        } else {
            mProgressBar?.visibility = View.GONE
        }
    }


    /**
     * Location Request Callback.
     * To be handled in the Fragment
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        Log.d("fdl", "onRequestPermissionsResult")
        if (DeviceHelper.processPermissionsResult(requestCode, grantResults, requireContext())) {
            readLocation()
        }
    }


}
