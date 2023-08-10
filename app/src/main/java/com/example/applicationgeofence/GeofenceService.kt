package com.example.applicationgeofence

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import java.util.ArrayList

class GeofenceService:Service() {
    lateinit var geoFenceHelper:GeofenceHelper
    lateinit var geofencingClient: GeofencingClient
    private lateinit var sp: SharedPreferences
    private lateinit var speditor: SharedPreferences.Editor

    lateinit var latlongList : ArrayList<LatLng>

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        geoFenceHelper = GeofenceHelper(applicationContext)
        geofencingClient =  LocationServices.getGeofencingClient(applicationContext)

        startGeofence()

        return START_STICKY
    }

    private fun startGeofence() {
        val geofenceRequest = geoFenceHelper.getGeofenceRequest()
        val pendingIntent = geoFenceHelper.mGeofencePendingIntent
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            geofencingClient.addGeofences(geofenceRequest,pendingIntent!!)
                .addOnSuccessListener {
                    //initialise()
                    val gson = Gson()
                    speditor.remove("latlnglist")
                    speditor.apply()

                    //save new latlnglist
                    val json = gson.toJson(latlongList)
                    speditor.putString("latlnglist", json)
                    speditor.apply()
                    startService(
                        Intent(
                            this,
                            NotificationHelper::class.java
                        ).putExtra("desc", "Geofencing started..")
                    )

                }
                .addOnFailureListener {
                    //val error = getErrorString(it)
                   Log.i("applicationContext", "Error ")
                }
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        removeGeofence()
    }

    private fun removeGeofence() {
        geofencingClient.removeGeofences(geoFenceHelper.getGeofencePendingIntent()!!)
            .addOnSuccessListener {
                startService(
                    Intent(
                        this,
                        NotificationHelper::class.java
                    ).putExtra("desc", "Geofencing has stopped")
                )
            }
            .addOnFailureListener {
                Toast.makeText(getApplicationContext()
                    , "Geofencing could not be removed", Toast.LENGTH_SHORT).show()
            }

    }

}