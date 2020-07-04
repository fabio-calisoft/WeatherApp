package com.fabio.weatherapp.view

import android.R.attr.animationDuration
import android.os.Bundle
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
import com.fabio.weatherapp.R
import com.fabio.weatherapp.databinding.FragmentDetailsBinding
import com.fabio.weatherapp.viewmodel.DetailsActivityViewModel
import kotlinx.android.synthetic.main.fragment_details.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
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
        woeid = arguments?.getInt("WOEID")
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
        viewModel.response.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                Log.d("fdl", "consolidated_weather: ${it.consolidated_weather[0]}")

                val roundedTemperature = it.consolidated_weather[0].the_temp.roundToInt()
                text_temperature.text = "$roundedTemperature \u2103"
                text_main_weather.text = it.consolidated_weather[0].weather_state_name
                val updatedTime = convertUTC_to_local_Timezone(it.consolidated_weather[0].created)
                text_last_update.text = "last updated: $updatedTime"

                text_pressure.text = "${it.consolidated_weather[0].air_pressure} mb"
                text_humidity.text = "${it.consolidated_weather[0].humidity} %"
                val roundedVisibility = it.consolidated_weather[0].visibility.roundToInt()
                text_visibility.text = "$roundedVisibility mi"
                val roundedWindSpeed = it.consolidated_weather[0].wind_speed.roundToInt()
                text_wind.text =
                    "$roundedWindSpeed knt (${it.consolidated_weather[0].wind_direction_compass})"

                convertWeatherStateToDrawableName(it.consolidated_weather[0].weather_state_abbr)?.let { id ->
                    image_icon.setImageResource(id)
                }


                // Wind Arrow
                val windDirection = it.consolidated_weather[0].wind_direction
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

    /**
     * Takes the state of the weather and return the appropriate id
     * of the drawable or null if the state is not found
     */
    private fun convertWeatherStateToDrawableName(weather_state_abbr: String): Int? {
        return when (weather_state_abbr) {
            "c" -> R.drawable.ic_c
            "h" -> R.drawable.ic_h
            "hc" -> R.drawable.ic_hc
            "hr" -> R.drawable.ic_hr
            "lc" -> R.drawable.ic_lc
            "lr" -> R.drawable.ic_lr
            "s" -> R.drawable.ic_s
            "sl" -> R.drawable.ic_sl
            "sn" -> R.drawable.ic_sn
            "t" -> R.drawable.ic_t
            else -> null
        }

    }

    fun convertUTC_to_local_Timezone(sourceDate: String): String {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        input.timeZone = TimeZone.getTimeZone("UTC")
        val output = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US)
        output.timeZone = TimeZone.getDefault()

        var d: Date? = null
        try {
            d = input.parse(sourceDate) // "2018-02-02T06:54:57.744Z"
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val formatted = output.format(d)
        Log.i("DATE", "" + formatted)
        return formatted
    }


}