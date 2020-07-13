package com.fabio.weatherapp

import android.text.TextUtils
import android.util.Log
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


object DateHelper {


    @JvmStatic
    fun convertUTC_to_local_Timezone(sourceDate: String): String {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        input.timeZone = TimeZone.getTimeZone("UTC")
        val output = if (sourceDate.startsWith("2020")) {
            SimpleDateFormat("EEEE MMMM dd, HH:mm", Locale.US)
        } else {
            SimpleDateFormat("EEEE MMMM dd YYYY, HH:mm", Locale.US)
        }
        output.timeZone = TimeZone.getDefault()

        var d: Date? = null
        try {
            d = input.parse(sourceDate) // "2018-02-02T06:54:57.744Z"
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        d?.let {
            return output.format(it)
        }
        return ""
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
        d?.let {
            return output.format(it)
        }
        return ""
    }

    @JvmStatic
    fun extractTime(sourceDate: String): String {
        Log.d("fdl.extractTime", "sourceDate: $sourceDate")
        if (TextUtils.isEmpty(sourceDate) || sourceDate.length < 16)
            return ""
        val extracted = sourceDate.substring(11, 16)
        Log.d("fdl.extractTime", "extracted: $extracted")
        return extracted

    }


    @JvmStatic
    fun getTodayDate(): Map<String, Int> {
        val li: HashMap<String, Int> = HashMap()
        li["day"] = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        li["month"] = Calendar.getInstance().get(Calendar.MONTH) + 1
        li["year"] = Calendar.getInstance().get(Calendar.YEAR)
        Log.d("fdl", "getTodayDate ,m/d/y ${li["month"]}/${li["day"]}/${li["year"]}")
        return li
    }

    /**
     * returns today date String in a form of yyyy-mm-dd
     */
    @JvmStatic
    fun getDashFormattedTodayDate(): String {
        val mFormat = DecimalFormat("00")
        val day = mFormat.format(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
        val month = mFormat.format(Calendar.getInstance().get(Calendar.MONTH) + 1)
        val year = mFormat.format(Calendar.getInstance().get(Calendar.YEAR))
        val res = "$year-$month-$day"
        Log.d("fdl", "getDashFormattedTodayDate: $res")
        return res
    }

}


