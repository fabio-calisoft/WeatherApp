package com.fabio.weatherapp.view

import android.location.Criteria
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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fabio.weatherapp.DeviceHelper
import com.fabio.weatherapp.DeviceHelper.checkLocationPermission
import com.fabio.weatherapp.DeviceHelper.processPermissionsResult
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

    private val TAG: String = SearchCityFragment::class.java.name


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
                    Log.d(TAG, "setLocation ${mLoc.title}")
                }
                adapter.setLocation(aList)
                text_no_results.visibility = if (aList.isEmpty()) View.VISIBLE else View.INVISIBLE
            }
            if (lastSearchStringSubmitted == mSearchEdt.text.toString()) {
                Log.d(TAG, "lastSearchStringSubmitted is as submitted")
                lastSearchStringSubmitted = ""
            } else {
                lastSearchStringSubmitted = mSearchEdt.text.toString()
                viewModel.searchLocationByName(mSearchEdt.text.toString())
            }

        })
        adapter = SearchAdapter(this)
        rvSearch.adapter = adapter

        mSearchEdt.addTextChangedListener(this)

        mSearchEdt.setOnEditorActionListener { _, actionId, _ ->
            Log.d(TAG, "setOnEditorActionListener actionId:$actionId")
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                searchImage(mSearchEdt.text.toString())
                DeviceHelper.hideKeyboard(activity)
            }
            false
        }

    }


    override fun afterTextChanged(p0: Editable?) {
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        Log.d(TAG, "onTextChanged: $p0")
        if (lastSearchStringSubmitted.isEmpty()) {
            lastSearchStringSubmitted = mSearchEdt.text.toString()
            viewModel.searchLocationByName(mSearchEdt.text.toString())
        }
    }

    fun cleanSearchView() {
        Log.d(TAG, "clean")
        mSearchEdt.text.clear()
        text_no_results.visibility = View.INVISIBLE
    }


    fun readLocation() {
        Log.d(TAG, "readLocation")

        val locationManager =
            activity?.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        val c = Criteria()
        val provider = locationManager.getBestProvider(c, false)

        activity?.let {
            if (checkLocationPermission(it, this)) {
                Log.d(TAG, "readLocation granted")
                val locationManager =
                    it.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
                manageProgressBar(true, "Reading gps location")

                val location = locationManager.getLastKnownLocation(provider!!)
                if (location != null) {
                    updateLocation(location.latitude, location.longitude)
                    manageProgressBar(false, "Reading gps location")
                } else {
                    Log.w(
                        TAG,
                        "No provider :( Let's try with GPS. This is going to take some time !!"
                    )
                    // No provider :( Let's try with GPS. This is going to take some time !!
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 5 * 60 * 1000, 100f,

                        object : LocationListener {
                            override fun onLocationChanged(location: Location) {
                                Log.d(
                                    TAG, "onLocationChanged Lat: " + location.latitude + " Lng: "
                                            + location.longitude
                                )
                                updateLocation(location.latitude, location.longitude)
                                manageProgressBar(false, "Reading gps location")
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
                }

            } else {
                Log.w(TAG, "readLocation NOT granted...")
            }
        }


    }


    fun updateLocation(latitude: Double, longitude: Double) {
        viewModel.searchLocationByCoordinates(
            latitude,
            longitude
        )
        manageProgressBar(false, "Reading gps location")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionsResult")
        if (processPermissionsResult(requestCode, grantResults, requireContext())) {
            readLocation()
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


}