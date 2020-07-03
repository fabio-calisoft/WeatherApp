package com.fabio.weatherapp.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fabio.weatherapp.R
import com.fabio.weatherapp.adapter.SearchAdapter
import com.fabio.weatherapp.helper.DeviceHelper
import com.fabio.weatherapp.viewmodel.SearchActivityViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.search_container.*

class MainActivity : AppCompatActivity(), TextWatcher {

    private lateinit var viewModel:SearchActivityViewModel

    private lateinit var adapter: SearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("fdl", "onCreate")

        setContentView(R.layout.activity_main)
        viewModel= ViewModelProvider(this).get(SearchActivityViewModel::class.java)

//        ivSearch.setOnClickListener {
//          viewModel.searchLocation(mSearchEdt.text.toString())
//        }

        viewModel.showProgress.observe(this, Observer {
            if(it){
                pgSearch.visibility= View.VISIBLE
            }
            else{
                pgSearch.visibility= View.GONE
            }
        })

        viewModel.locationList.observe(this, Observer {
            it.forEach { mLoc ->
                Log.d("fdl", "setLocation ${mLoc.title}")
            }
          adapter.setLocation(it)
        })
        adapter=SearchAdapter(this)
        rvSearch.adapter=adapter


        mSearchEdt.setOnClickListener {
            mSearchEdt.text?.clear()
            mSearchEdt.requestFocus()
            DeviceHelper.showKeyboard(mSearchEdt, this)
        }
        mSearchEdt.addTextChangedListener(this)

        mSearchEdt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                searchImage(mSearchEdt.text.toString())
                DeviceHelper.hideKeyboard(this)
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
        viewModel.searchLocation(mSearchEdt.text.toString())
    }


}



