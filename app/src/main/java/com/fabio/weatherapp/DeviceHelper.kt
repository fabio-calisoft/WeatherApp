package com.fabio.weatherapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


object DeviceHelper {


    @JvmStatic
    fun hideKeyboard(activity: Activity?) {
        if (activity != null) {
            // Check if no view has focus:
            val view = activity.currentFocus
            if (view != null) {
                val imm = activity.getSystemService(
                    Context.INPUT_METHOD_SERVICE
                ) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    }

    @JvmStatic
    fun showKeyboard(v: View, context: Context) {
        val imm = context.getSystemService(
            Context.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
    }


    /**
     * Takes the state of the weather and return the appropriate id
     * of the drawable or null if the state is not found
     */
    @JvmStatic
    fun convertWeatherStateToDrawableName(weather_state_abbr: String): Int? {
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


}


