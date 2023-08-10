
package com.example.applicationgeofence

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.model.LatLng
import java.util.ArrayList

class GeofenceHelper(context: Context) {
    lateinit var mGeoFenceList : ArrayList<Geofence>
    lateinit var latlongList : ArrayList<LatLng>
    lateinit var geofence:Geofence
    private val Geo_Fence_Radius = 1100.0f
    var mGeofencePendingIntent: PendingIntent? = null
    var ctx = context


    fun getGeofenceRequest(): GeofencingRequest {
        mGeoFenceList = ArrayList()

        for (coordinate in latlongList!!) {
            geofence =   getGeofence(coordinate)
            mGeoFenceList!!.add(geofence)

        }

        val builder = GeofencingRequest.Builder()

        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_EXIT or GeofencingRequest.INITIAL_TRIGGER_DWELL)

        builder.addGeofences(mGeoFenceList!!)

        return builder.build()
    }

    fun getGeofencePendingIntent(): PendingIntent? {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent
        }
        val intent = Intent(ctx, MyBroadcastReceiver::class.java).putExtra("Action", "GeoFence")

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.S){
            mGeofencePendingIntent = PendingIntent.getBroadcast(ctx, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        }else{
            mGeofencePendingIntent = PendingIntent.getBroadcast(ctx, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        return mGeofencePendingIntent
    }

    fun getGeofence(coordinate: LatLng): Geofence {
       return Geofence.Builder().setRequestId("My Geofence ID") // A string to identify this geofence
            .setCircularRegion(
                coordinate.latitude,
                coordinate.longitude,
                Geo_Fence_Radius
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setLoiteringDelay(11)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()
    }

}