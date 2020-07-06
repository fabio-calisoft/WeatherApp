package com.fabio.weatherapp.view

import android.R.attr.animationDuration
import android.content.Context
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
//    private var woeid: Int? = 44418

    private lateinit var binding: FragmentDetailsBinding

    private var calendar_year: Int? = null
    private var calendar_month: Int? = null
    private var calendar_day: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("fdl.DetailsFragment", "onCreateView")
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

        viewModelNetwork.locationList.observe(viewLifecycleOwner, Observer {
            it?.let { aList ->
                aList.forEach { mLoc ->
                    Log.d("fdl", "Location found: ${mLoc.title}")
                }
                woeid = aList[0].woeid
                tv_locationName.text = aList[0].title
                woeid?.let { mWoeid ->
                    // save woeid into SP
                    val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
                    sharedPref?.let { sp ->
                        with(sp.edit()) {
                            Log.d("fdl", "saving WOEID in SP:$mWoeid")
                            putInt("WOEID", mWoeid)
                            putString("LOCATION_NAME", aList[0].title)
                            commit()
                        }

                    }
                    Log.d("fdl.DetailsFragment", "onCreateView woeid:$woeid")

                    // get the weather for this Location
                    viewModel.getWeather(mWoeid)
                }
            }

        })

        // SwipeRefreshLayout
        swipe_container.setOnRefreshListener {
            Log.d("fdl", "refresh initiated")
            // dispatches execution into Android main thread
            val uiScope = CoroutineScope(Dispatchers.Main)
            uiScope.launch {
                woeid?.let {
                    viewModel.getWeather(it)
                }
                swipe_container.isRefreshing = false
                Log.d("fdl", "refresh stop")
            }
        }


        //TODO DEBUG
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        arguments?.let {
            if (it.containsKey("WOEID")) {
                woeid = arguments?.getInt("WOEID")
                tv_locationName.text = arguments?.getString("LOCATION_NAME")
                sharedPref?.let { sp ->
                    with(sp.edit()) {
                        Log.d("fdl", "saving WOEID in SP:$woeid")
                        putInt("WOEID", woeid!!)
                        putString("LOCATION_NAME", arguments?.getString("LOCATION_NAME"))
                        commit()
                    }

                }


            }

            if (it.containsKey("YEAR") && it.containsKey("MONTH") && it.containsKey("DAY")) {
                calendar_year = arguments?.getInt("YEAR")
                calendar_month = arguments?.getInt("MONTH")
                calendar_day = arguments?.getInt("DAY")
            }

        }
        Log.d("fdl", "calendar_year:$calendar_year")
        Log.d("fdl", "calendar_month:$calendar_month")
        Log.d("fdl", "calendar_day:$calendar_day")

        // If I don't have a location (woeid) yet, let's check in SharedPref.
        // If empty, let's query Location Manager
        if (woeid == null) {
            // do I have the location in Shared Preferences ?
            if (sharedPref != null && sharedPref.contains("WOEID")) {
                // ok, I have woeid from SP
                woeid = sharedPref.getInt("WOEID", -1)
                if (woeid == -1) {
                    Log.e("fdl", "cannot read from SP")
                    woeid = null
                } else {
                    tv_locationName.text = sharedPref.getString("LOCATION_NAME", " --- ")
                }
                Log.d("fdl", "SharedPreferences woeid:$woeid")
            } else {
                // I don't have woeid. let's use Location Manager
                Log.d("fdl", "let's use coordinates from Location Manager")
                readLocation()
            }

        }

        woeid?.let {
            Log.d("fdl", "x calendar_year;$calendar_year")
            Log.d("fdl", "x calendar_month;$calendar_month")
            Log.d("fdl", "x calendar_day;$calendar_day")
            // Check if it's coming from the calendar POST
            if (calendar_year != null && calendar_month != null && calendar_day != null &&
                calendar_year!! > 0 && calendar_month!! > 0 && calendar_day!! > 0
            ) {
                // Get Weather some date in the past
                viewModel.getWeatherForDate(
                    it,
                    calendar_year!!,
                    calendar_month!!,
                    calendar_day!!
                )
                calendar_year = null
                calendar_month = null
                calendar_day = null
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

    fun navigateToSearchLocation() {
        parentFragment?.findNavController()?.navigate(R.id.detailsFragment_to_searchCityFragment)
    }


    // ===== GPS Location Manager
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

    fun openCalendar() {
        parentFragment?.findNavController()
            ?.navigate(R.id.action_detailsFragment_to_calendarFragment)
    }


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
