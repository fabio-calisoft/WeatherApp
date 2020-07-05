package com.fabio.weatherapp.view

import android.R.attr.animationDuration
import android.icu.util.Calendar
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
import com.fabio.weatherapp.DeviceHelper.checkLocationPermission
import com.fabio.weatherapp.DeviceHelper.convertWeatherStateToDrawableName
import com.fabio.weatherapp.R
import com.fabio.weatherapp.adapter.ForecastAdapter
import com.fabio.weatherapp.databinding.FragmentDetailsBinding
import com.fabio.weatherapp.model.ConsolidatedWeather
import com.fabio.weatherapp.viewmodel.DetailsActivityViewModel
import com.fabio.weatherapp.viewmodel.SearchActivityViewModel
import kotlinx.android.synthetic.main.fragment_details.*
import ru.cleverpumpkin.calendar.CalendarDate
import ru.cleverpumpkin.calendar.CalendarView
import kotlin.math.roundToInt


class DetailsFragment : Fragment() {

    private lateinit var viewModel: DetailsActivityViewModel
    private lateinit var viewModelNetwork: SearchActivityViewModel
    private var woeid: Int? = null
    private var locationName: String? = null
    private lateinit var binding: FragmentDetailsBinding
    private lateinit var calendarView: CalendarView

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

        calendarView = view.findViewById(R.id.calendar_view)
        viewModel = ViewModelProvider(this).get(DetailsActivityViewModel::class.java)
        viewModelNetwork = ViewModelProvider(this).get(SearchActivityViewModel::class.java)

        tv_locationName.text = locationName



        viewModel.showProgress.observe(viewLifecycleOwner, Observer {
            manageProgressBar(it)
        })

        viewModelNetwork.showProgress.observe(viewLifecycleOwner, Observer {
            manageProgressBar(it)
        })

        viewModelNetwork.locationList.observe(viewLifecycleOwner, Observer {
            it?.let { aList ->
                aList.forEach { mLoc ->
                    Log.d("fdl", "Location found: ${mLoc.title}")
                }
                woeid = aList[0].woeid
                tv_locationName.text = aList[0].title
                woeid?.let {
                    viewModel.getWeather(aList[0].woeid)
                }
            }

        })


        // If I don't have a location (woeid) yet, let's query Location Manager
        if (woeid == null || TextUtils.isEmpty(locationName)) {
            // let's use Location Manager
            Log.d("fdl", "let's use coordinates saved from Location Manager")
            readLocation()
//            // save last location in SharedPreferences
//            val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
//            val latitude = sharedPref.getFloat("lat", 0f)
//            val longitude = sharedPref.getFloat("long", 0f)
//            Log.d("fdl", "latitude:$latitude longitude:$longitude")

        } else {
            woeid?.let {
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


        // calendar
        setupCalendar()
        imageViewCalendar.setOnClickListener {
            Log.d("fdl", "show.calendar")
            calendar_view.visibility = View.VISIBLE
        }

        viewModel.hourlyResponse.observe(viewLifecycleOwner, Observer {
            // received data for a day in the past
            if (it != null) {
                Log.d("fdl", "single ConsolidatedWeather: $it")
                updateTodayWeatherUI(it)

                // remove forecast
                forecastRV.visibility = View.INVISIBLE
                text_forecast.visibility = View.INVISIBLE
            }


        })



        viewModel.response.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                Log.d("fdl", "consolidated_weather: ${it.consolidated_weather[0]}")

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

    private fun readLocation() {
        Log.d("fdl", "readLocation")

        activity?.let {
            if (checkLocationPermission(it)) {
                Log.d("fdl", "readLocation granted")
                val locationManager =
                    it.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
                manageProgressBar(true)
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 5000, 10f,

                    object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            Log.d(
                                "fdl", "onLocationChanged Lat: " + location.latitude + " Lng: "
                                        + location.longitude
                            )
                            viewModelNetwork.searchLocationByCoordinates(
                                location.latitude,
                                location.longitude
                            )
                            manageProgressBar(false)

                        }


                        override fun onStatusChanged(
                            provider: String,
                            status: Int,
                            extras: Bundle
                        ) {
                        }

                        override fun onProviderEnabled(provider: String) {}
                        override fun onProviderDisabled(provider: String) {}
                    }

                )
            } else {
                Log.w("fdl", "readLocation NOT granted...")
            }
        }


    }


    private fun manageProgressBar(isActive: Boolean) {
        if (isActive) {
            mProgressBar.visibility = View.VISIBLE
        } else {
            mProgressBar.visibility = View.GONE
        }
    }

    private fun setupCalendar() {

        val calendar = Calendar.getInstance()

        // Initial date
        calendar.set(2018, Calendar.JUNE, 1)
        val initialDate = CalendarDate(calendar.time)

        // Minimum available date
        calendar.set(2014, Calendar.JANUARY, 1)
        val minDate = CalendarDate(calendar.time)

        // get today date
        val today = DateHelper.getTodayDate()

        // Maximum available date
        calendar.set(today["year"] ?: 1973, today["month"] ?: 9, today["day"] ?: 11)
        val maxDate = CalendarDate(calendar.time)

        // The first day of week
        val firstDayOfWeek = java.util.Calendar.MONDAY

        // Set up calendar with all available parameters
        calendarView.setupCalendar(
            selectionMode = CalendarView.SelectionMode.SINGLE,
            initialDate = initialDate,
            minDate = minDate,
            maxDate = maxDate,
            firstDayOfWeek = firstDayOfWeek,
            showYearSelectionView = true
        )

        // Set date click callback
        calendarView.onDateClickListener = { date ->
            val selectedDates = calendarView.selectedDates
            Log.d("fdl.calendar", "woied:$woeid selectedDates:$selectedDates")
            calendar_view.visibility = View.GONE
            woeid?.let {
                viewModel.getWeatherForDate(it, date.year, (date.month+1), date.dayOfMonth)
            }
        }

        // Set date long click callback
        calendarView.onDateLongClickListener = { date ->
            Log.d("fdl.calendar", "date:$date")
            // Do something ...
        }
    }


}
