package com.example.connectivitygeofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.connectivitygeofence.App.Companion.CHANNEL_1_ID

class GeoFenceBroadcastReceiver : BroadcastReceiver() {
    companion object{
        const val TAG = "GeoFenceBroadcastReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.

        val notificationManager = NotificationManagerCompat.from(context)

        sendNotification(notificationManager, context)
        Log.d(TAG, "Estas en la geofence")
    }

    private fun sendNotification(notificationManager: NotificationManagerCompat, context: Context){
        val title = context.getString(R.string.app_name)
        val message = "Estas en la geofence"
        val notification = NotificationCompat.Builder(context, CHANNEL_1_ID)
            .setSmallIcon(R.drawable.ic_bluetooth_black_24dp)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()
        notificationManager.notify(1, notification)
    }
}
