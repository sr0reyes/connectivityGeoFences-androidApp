package com.example.connectivitygeofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class GeoFenceBroadcastReceiver : BroadcastReceiver() {
    companion object{
        const val TAG = "GeoFenceBroadcastReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        Log.d(TAG, "Estas en la geofence")
    }
}
