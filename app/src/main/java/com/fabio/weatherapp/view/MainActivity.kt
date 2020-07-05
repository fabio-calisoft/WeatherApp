package com.fabio.weatherapp.view

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fabio.weatherapp.R


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("fdl", "onCreate")

        setContentView(R.layout.activity_main)

//        readLocation()
    }

    fun readLocation() {
        Log.d("fdl", "readLocation")

        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val locationListener: LocationListener = MyLocationListener(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("fdl", "adk for")
            ActivityCompat.requestPermissions(this, arrayOf(LOCATION_SERVICE, ACCESS_FINE_LOCATION), 123)
            return
        }
        Log.d("fdl", "granted")

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER, 5000, 10f, locationListener
        )
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d("fdl", "granted")
        grantResults.forEach {
            if (it != PackageManager.PERMISSION_GRANTED) {
                Log.e("fdl", "NO granted")
            } else {
                Log.i("fdl", "granted")
            }
        }
    }


}



