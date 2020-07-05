package com.fabio.weatherapp

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*


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

    @JvmStatic
    fun convertUTC_to_local_Timezone(sourceDate: String): String {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        input.timeZone = TimeZone.getTimeZone("UTC")
        val output = SimpleDateFormat("EEEE MMMM dd, HH:mm", Locale.US)
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

    /**
     * takes a yyyy-mm-dd and returns a DayName dd mm yyyy
     */
    @JvmStatic
    fun convertDate(sourceDate: String): String {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        input.timeZone = TimeZone.getTimeZone("UTC")
        val output = SimpleDateFormat("EEEE", Locale.US)
        output.timeZone = TimeZone.getDefault()

        var d: Date? = null
        try {
            d = input.parse(sourceDate) // "2018-02-02T06:54:57.744Z"
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return output.format(d)
    }

    @JvmStatic
    fun extractTime(sourceDate: String): String {
        Log.d("fdl.extractTime", "sourceDate: $sourceDate")
        if (TextUtils.isEmpty(sourceDate) || sourceDate.length<16)
            return ""
        val extracted = sourceDate.substring(11,16)
        Log.d("fdl.extractTime", "extracted: $extracted")
        return extracted

    }


}


