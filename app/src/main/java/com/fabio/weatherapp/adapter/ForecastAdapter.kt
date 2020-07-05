package com.fabio.weatherapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fabio.weatherapp.DateHelper.convertDate
import com.fabio.weatherapp.DeviceHelper
import com.fabio.weatherapp.R
import com.fabio.weatherapp.model.ConsolidatedWeather
import kotlinx.android.synthetic.main.rv_forecast_item.view.*
import kotlin.math.roundToInt

class ForecastAdapter :
    RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder>() {

    var weatherDataList: List<ConsolidatedWeather> = emptyList()


    inner class ForecastAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastAdapterViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.rv_forecast_item, parent, false)
        return ForecastAdapterViewHolder(view)
    }

    override fun getItemCount(): Int {
        return weatherDataList.size
    }

    override fun onBindViewHolder(holder: ForecastAdapterViewHolder, position: Int) {
        val currLocation = weatherDataList[position]
        val roundedWindSpeed = currLocation.wind_speed.roundToInt()
        val roundedVisibility = currLocation.visibility.roundToInt()
        val roundedHumidity = currLocation.humidity.roundToInt()
        val roundedAirPressure = currLocation.air_pressure.roundToInt()
        val roundedTemp = currLocation.the_temp.roundToInt()

        DeviceHelper.convertWeatherStateToDrawableName(currLocation.weather_state_abbr)
            ?.let { id ->
                holder.itemView.image_weather.setImageResource(id)
            }
        holder.itemView.text_forecast_date.text = convertDate(currLocation.applicable_date)
        holder.itemView.text_main_weather.text = currLocation.weather_state_name
        holder.itemView.text_forecast_temperature.text =
            holder.itemView.resources.getString(R.string.temperature, roundedTemp.toString())
        holder.itemView.text_forecast_wind.text = "Wind speed $roundedWindSpeed knt"
        holder.itemView.text_forecast_humidity.text = "Humidity: $roundedHumidity %"
        holder.itemView.text_forecast_pressure.text = "Pressure: $roundedAirPressure mb"
        holder.itemView.text_forecast_visibility.text = "Visibility: $roundedVisibility mi"
    }
}