package com.example.connectivitygeofence

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.connectivitygeofence.MyClasses.MyGeoFence
import kotlinx.android.synthetic.main.layout_item_geofence.view.*

class RecyclerViewAdapter(geofencesList: MutableList<MyGeoFence>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var geofencesItems: MutableList<MyGeoFence> = geofencesList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_item_geofence, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val geofence = geofencesItems[position]
        val locationString =  geofence.address
        val actionString = geofence.actionName

        when(holder){
            is ViewHolder ->
                holder.bind(locationString, actionString)
        }

    }

    override fun getItemCount(): Int {
        return geofencesItems.size
    }


    class ViewHolder(v: View): RecyclerView.ViewHolder(v) {

        val location_tv = v.location_tv
        val action_tv = v.action_tv

        fun bind(location: String, action: String){
            location_tv.text = location
            action_tv.text = action

        }

    }


}