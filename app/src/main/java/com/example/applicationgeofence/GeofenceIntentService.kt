package com.example.applicationgeofence

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlin.math.log

class GeofenceIntentService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        val geoFenceEvent = GeofencingEvent.fromIntent(intent)
    }

    companion object {
        private const val LOG_TAG = "GeoTrIntentService"
        private const val JOB_ID = 573

        @JvmStatic

        fun enqueueWork(
            context: Context,
            intent: Intent,
        ) {
            enqueueWork(
                context,
                GeofenceIntentService::class.java, JOB_ID,
                intent)

        }
    }


}