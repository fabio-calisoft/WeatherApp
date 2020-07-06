package com.fabio.weatherapp.view

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fabio.weatherapp.DeviceHelper
import com.fabio.weatherapp.R
import com.fabio.weatherapp.adapter.SearchAdapter
import com.fabio.weatherapp.databinding.FragmentSearchCityBinding
import com.fabio.weatherapp.viewmodel.SearchActivityViewModel
import kotlinx.android.synthetic.main.fragment_search_city.*
import kotlinx.android.synthetic.main.loading_progress.view.*


class SearchCityFragment : Fragment(), TextWatcher {

    private lateinit var viewModel: SearchActivityViewModel

    private lateinit var adapter: SearchAdapter
    private lateinit var binding: FragmentSearchCityBinding
    private var lastSearchStringSubmitted = "" // used to debounce fast typing in the searchview


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_city, container, false)
        binding.fragment = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel = ViewModelProvider(this).get(SearchActivityViewModel::class.java)

        viewModel.showProgress.observe(viewLifecycleOwner, Observer {
            if (it) {
                manageProgressBar(true, "Loading...")
            } else {
                manageProgressBar(false, "")
            }
        })

        viewModel.locationList.observe(viewLifecycleOwner, Observer {
            it?.let { aList ->
                aList.forEach { mLoc ->
                    Log.d("fdl", "setLocation ${mLoc.title}")
                }
                adapter.setLocation(aList)
            }
            if (lastSearchStringSubmitted == mSearchEdt.text.toString()) {
                Log.d("xxx", "lastSearchStringSubmitted is as submitted")
                lastSearchStringSubmitted = ""
            } else {
                Log.d(
                    "xxx",
                    "lastSearchStringSubmitted is different. I have to re-search for:${mSearchEdt.text}"
                )
                lastSearchStringSubmitted = mSearchEdt.text.toString()
                viewModel.searchLocationByName(mSearchEdt.text.toString())
            }

        })
        adapter = SearchAdapter(this)
        rvSearch.adapter = adapter

        mSearchEdt.addTextChangedListener(this)

        mSearchEdt.setOnEditorActionListener { _, actionId, _ ->
            Log.d("fdl", "setOnEditorActionListener actionId:$actionId")
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                searchImage(mSearchEdt.text.toString())
                DeviceHelper.hideKeyboard(activity)
            }
            false
        }

    }


    override fun afterTextChanged(p0: Editable?) {
        Log.d("fdl", "afterTextChanged")
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        Log.d("fdl", "beforeTextChanged")
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        Log.d("fdl", "onTextChanged: $p0")
        if (lastSearchStringSubmitted.isEmpty()) {
            Log.d("xxx", "lastSearchStringSubmitted is empty. Search for ${mSearchEdt.text}")
            lastSearchStringSubmitted = mSearchEdt.text.toString()
            viewModel.searchLocationByName(mSearchEdt.text.toString())
        } else {
            Log.d("xxx", "lastSearchStringSubmitted is not empty. Still doing some work")
        }

    }

    fun cleanSearchView() {
        Log.d("fdl", "clean")
        mSearchEdt.text.clear()
    }


    fun readLocation() {
        Log.d("fdl", "readLocation")

        activity?.let {
            if (checkLocationPermission(it)) {
                Log.d("fdl", "readLocation granted")
                val locationManager =
                    it.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
                manageProgressBar(true, "Reading gps location")
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 5000, 10f,

                    object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            Log.d(
                                "fdl", "onLocationChanged Lat: " + location.latitude + " Lng: "
                                        + location.longitude
                            )
                            viewModel.searchLocationByCoordinates(
                                location.latitude,
                                location.longitude
                            )
                            manageProgressBar(false, "Reading gps location")
                            // save coordinate into SP
                            val sharedPref =
                                activity?.getPreferences(Context.MODE_PRIVATE) ?: return
                            with(sharedPref.edit()) {
                                putFloat("lat", location.latitude.toFloat())
                                putFloat("long", location.longitude.toFloat())
                                commit()
                            }
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


    private fun manageProgressBar(isActive: Boolean, message: String) {
        if (isActive) {
            pgSearch?.let {
                it.textViewMessage.text = message
                pgSearch?.visibility = View.VISIBLE
            }
        } else {
            pgSearch?.visibility = View.INVISIBLE
        }
    }


    private val LOCATION_REQUEST_CODE_ID = 123

    fun checkLocationPermission(activity: Activity): Boolean {
        return if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_REQUEST_CODE_ID
                )
            } else {
                // No explanation needed, we can request the permission.
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_REQUEST_CODE_ID
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
        Log.d("fdl", "onRequestPermissionsResult")
        when (requestCode) {
            LOCATION_REQUEST_CODE_ID -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    // permission was granted, yay! Do the location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        //Request location updates:
                        Log.i("fdl", "granted")
                        readLocation()
                    }
                } else {
                    Log.e("fdl", "NO granted. Let's skip the Location Service")
                }
                return
            }
        }
    }


}