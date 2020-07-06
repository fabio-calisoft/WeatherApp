package com.fabio.weatherapp.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fabio.weatherapp.R
import com.fabio.weatherapp.adapter.SearchAdapter
import com.fabio.weatherapp.databinding.FragmentSearchCityBinding
import com.fabio.weatherapp.DeviceHelper
import com.fabio.weatherapp.viewmodel.SearchActivityViewModel
import kotlinx.android.synthetic.main.fragment_search_city.*


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
                pgSearch.visibility = View.VISIBLE
            } else {
                pgSearch.visibility = View.GONE
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
                Log.d("xxx", "lastSearchStringSubmitted is different. I have to re-search for:${mSearchEdt.text}")
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


}