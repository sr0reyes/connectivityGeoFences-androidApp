package com.example.connectivitygeofence

import android.app.PendingIntent
import android.location.Address
import android.location.Geocoder
import com.google.android.gms.maps.model.Circle
import java.util.*

class MyGeoFence(id: Int, circle: Circle, action: Int, val address: String) {

    companion object geofencesList{
        val bluetoothGeoFences = mutableListOf<MyGeoFence>()

    }

    val latLng = circle.center
    val radius = circle.radius
    val geofenceId = id.toString()
    val pendingIntentCode = id
    val actionCode = action
    var actionName: String

    init {
        actionName = getActionName(action)
    }


    private fun getActionName(action: Int): String{
        when(action){
            0 -> return "Activar Bluetooth"
            1 -> return "Desactivar Bluetooth"
        }

        return "No valido"
    }
}