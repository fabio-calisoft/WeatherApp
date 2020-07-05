package com.fabio.weatherapp.view

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

    private val REQUEST_CODE_ID = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("fdl", "onCreate")

        // wait to read the location
        readLocation()
    }

    fun readLocation() {
        Log.d("fdl", "readLocation")

        if (checkLocationPermission()) {
            Log.d("fdl", "readLocation granted")
            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            val locationListener: LocationListener = MyLocationListener(this)
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 10f, locationListener)
        } else {
            Log.w("fdl", "readLocation NOT granted...")
        }

    }


    fun checkLocationPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    ACCESS_FINE_LOCATION
                )
            ) {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(ACCESS_FINE_LOCATION),
                    REQUEST_CODE_ID
                )
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this, arrayOf(ACCESS_FINE_LOCATION),
                    REQUEST_CODE_ID
                )
            }
            false
        } else {
            true
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_ID -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    // permission was granted, yay! Do the location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                            this,
                            ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        //Request location updates:
                        Log.i("fdl", "granted")
                        readLocation()
                    }
                } else {
                    Log.e("fdl", "NO granted. Let's skip the Location Service")
                    setContentView(R.layout.activity_main)
                }
                return
            }
        }
    }


}



