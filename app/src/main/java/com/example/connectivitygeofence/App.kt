package com.example.connectivitygeofence

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class App() : Application() {

    companion object{
        const val CHANNEL_1_ID = "channel1"
    }


    override fun onCreate() {
        super.onCreate()

        val notificationManager = getSystemService(NotificationManager::class.java)
        createChannels(notificationManager)
    }

    private fun createChannels(notificationManager: NotificationManager?){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel1 = NotificationChannel(
                App.CHANNEL_1_ID,
                "chanel 1",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel1.description = "This is the notification chanel 1"

            notificationManager?.createNotificationChannel(notificationChannel1)
        }
    }
}