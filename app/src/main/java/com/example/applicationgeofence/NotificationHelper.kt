package com.example.applicationgeofence

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import java.util.*

class NotificationHelper : Service() {
    var timer: Timer? = null
    val channel_ID = "Notification"
    lateinit var notificationbuilder: Notification.Builder
    lateinit var notificationManager : NotificationManager

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val desc  = intent!!.getStringExtra("desc")
        initializeTimerTask(desc)

        return START_STICKY
    }


    private fun initializeTimerTask(desc: String?) {

                    if(Build.VERSION.SDK_INT>Build.VERSION_CODES.O){
                        val name  = "Notification"
                        val descriptionText = "This notification is for Violations"
                        val importance = NotificationManager.IMPORTANCE_HIGH
                        val channel = NotificationChannel(channel_ID, name, importance).apply {
                            description  =  descriptionText
                        }

                         notificationManager =  applicationContext!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.createNotificationChannel(channel)
                    }

                    val intent = Intent(applicationContext, ActivityViolationsDetails::class.java).apply {
                    }

                    var pendingIntent  :PendingIntent

                    if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.S){
                        pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent,  PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

                    }else{
                        pendingIntent =PendingIntent.getActivity(applicationContext, 0, intent,  PendingIntent.FLAG_UPDATE_CURRENT)
                    }
                    if(Build.VERSION.SDK_INT>Build.VERSION_CODES.O){
                        notificationbuilder = Notification.Builder(applicationContext, channel_ID)
                    }else {
                        notificationbuilder = Notification.Builder(applicationContext)
                    }
                    notificationbuilder.setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle("Application GeoFence")
                        .setContentText(desc)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)

                    notificationbuilder.build()
                    notificationManager.notify(
                        System.currentTimeMillis().toInt(),
                        notificationbuilder.build()
                    )

    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }

    private fun stopTimer() {
        if (timer != null) {
            timer!!.cancel();
            timer = null
        }
    }

}