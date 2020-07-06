package com.fabio.weatherapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.fabio.weatherapp.R
import com.fabio.weatherapp.model.Location
import kotlinx.android.synthetic.main.rv_location_item.view.*

class SearchAdapter(private val parentFragment: Fragment) :
    RecyclerView.Adapter<SearchAdapter.SearchAdapterViewHolder>() {

    private var list: List<Location> = ArrayList()

    fun setLocation(list: List<Location>) {
        this.list = list
        notifyDataSetChanged()
    }

    inner class SearchAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchAdapterViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.rv_location_item, parent, false)
        return SearchAdapterViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: SearchAdapterViewHolder, position: Int) {
        val currLocation = list[position]
        holder.itemView.tv_locationName.text = currLocation.title
        holder.itemView.tv_latlong.text = currLocation.latt_long
        holder.itemView.root_view.setOnClickListener {
            val bundle = bundleOf(
                "WOEID" to currLocation.woeid,
                "LOCATION_NAME" to currLocation.title
            )
            parentFragment.findNavController()
                .navigate(R.id.action_searchCityFragment_to_detailsFragment, bundle)
        }
    }
}