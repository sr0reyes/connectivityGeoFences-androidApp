package com.example.connectivitygeofence

import android.location.Address
import android.location.Geocoder
import com.google.android.gms.maps.model.Circle
import java.util.*

class MyGeoFence(id: Int, circle: Circle, action: Int, location: String) {

    companion object geofencesList{
        val bluetoothGeoFences = mutableListOf<MyGeoFence>()

    }

    private val latLng = circle.center
    private val radius = circle.radius
    private val geofenceId = id.toString()
    private val pendingIntentId = id

    

}