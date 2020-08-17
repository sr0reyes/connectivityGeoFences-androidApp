package com.example.connectivitygeofence

import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.model.LatLng
import java.lang.Exception

class GeofenceHelper(base: Context?) : ContextWrapper(base) {

    private lateinit var pendingIntent: PendingIntent


    fun getGeoFencingRequest(geofence: Geofence): GeofencingRequest{
            return GeofencingRequest.Builder().apply {
                setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
                addGeofence(geofence)
            }.build()

    }

    fun getGeofence(id: String, latLng: LatLng, radius: Float, transitionTypes: Int): Geofence{
            return Geofence.Builder().apply {
                setRequestId(id)
                setCircularRegion(
                    latLng.latitude,
                    latLng.longitude,
                    radius
                )
                setExpirationDuration(Geofence.NEVER_EXPIRE)
                setTransitionTypes(transitionTypes)
                setLoiteringDelay(5000)
            }.build()

    }

    fun getPendingIntent(): PendingIntent{
        val intent = Intent(this, GeoFenceBroadcastReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(this, 2607,
            intent, PendingIntent.FLAG_UPDATE_CURRENT)

        return pendingIntent
    }

    fun getErrors(e: Exception): String?{
        if(e is ApiException){
            val apiException: ApiException = e;
            when(apiException.statusCode){
                GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE ->
                    return "GEOFENCE_NOT_AVAILABLE"
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES ->
                    return "GEOFENCE_NOT_AVAILABLE"
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS ->
                    return "GEOFENCE_NOT_AVAILABLE"
            }
        }
        return e.localizedMessage
    }

}