package com.fabio.weatherapp.helper

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager


object DeviceHelper {

    private var mScreenWidth = -1
    private var mScreenHeight = -1

    @JvmStatic
    val screenWidth: Int = Resources.getSystem().displayMetrics.widthPixels

    @JvmStatic
    val screenHeight: Int = Resources.getSystem().displayMetrics.heightPixels

    @JvmStatic
    fun getWindowHeight(activity: Activity): Int {
        val display = activity.windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.y
    }

    @JvmStatic
    fun getWindowWidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }

    @JvmStatic
    fun hideKeyboard(activity: Activity?) {
        if (activity != null) {
            // Check if no view has focus:
            val view = activity.currentFocus
            if (view != null) {
                val imm = activity.getSystemService(
                    Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    }

    @JvmStatic
    fun getDp(number: Int, context: Context): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, number.toFloat(), context.resources.displayMetrics).toInt()
    }

    val Int.dp: Int
        get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, toFloat(), Resources.getSystem().displayMetrics).toInt()

    val Float.dp: Float
        get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, toFloat(), Resources.getSystem().displayMetrics)

    @JvmStatic
    fun showKeyboard(v: View, context: Context) {
        val imm = context.getSystemService(
            Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
    }

}


