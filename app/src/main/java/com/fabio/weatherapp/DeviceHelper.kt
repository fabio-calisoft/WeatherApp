package com.fabio.weatherapp

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager


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

}


