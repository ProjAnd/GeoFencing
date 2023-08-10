package com.example.applicationgeofence

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.*


class MainActivity : AppCompatActivity() , LocationListener,
    OnMapReadyCallback, OnMapClickListener, OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener

{
    private val TAG = "MainActivity"
    private  val STORAGE_PERMISSION_CODE = 1
    private  val Background_PERMISSION_CODE = 2
    private  val PUSH_NOTIFICATION_CODE = 3
    private  var mLatitute = 28.7041
    private var mLongitude = 77.1025
    private val Geo_Fence_Radius = 1100.0f
    lateinit var mGoogleApiClient: GoogleApiClient
    private var mGeoFenceList: ArrayList<Geofence>?=null
    lateinit var geofencingClient: GeofencingClient

    var mGeofencePendingIntent: PendingIntent? = null

    private var gMap:GoogleMap?=null
    private var mapFragment:SupportMapFragment?=null
    private var lastKnownLocation:Location?=null
        private var marker:Marker?=null
        private var geofenceMarker:Marker?=null
    private  var ivLogout:ImageView?=null
    private var auth:FirebaseAuth?=null
    private  var alertDialog: AlertDialog?=null
    private lateinit var tvAddGeofence:TextView
    private lateinit var tvRemoveGeofence:TextView
    private lateinit var ivSetTime:ImageView


    private var latlongList:ArrayList<LatLng>?=null
    private lateinit var geofence:Geofence
    private lateinit var sp: SharedPreferences
    private lateinit var speditor: SharedPreferences.Editor
    private lateinit var c:Calendar

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i(TAG, "${TAG} :onCreate")
        initView()

        auth = FirebaseAuth.getInstance()
        sp = getSharedPreferences("myPrefs", MODE_PRIVATE)
        speditor = sp.edit()

        val intentfilter = IntentFilter()
        val br = Receiver()
        br.setMainActivityHandler(this)
        intentfilter.apply {
            addAction("AlarmStartGeoFence")
            addAction("AlarmStopGeoFence")
        }
        registerReceiver(br, intentfilter)

        checkStoragePermission()
        disablePowerOptization()
        initGMaps()
        createGoogleApiClient()

        mGeoFenceList = ArrayList()
        geofencingClient =  LocationServices.getGeofencingClient(this)
        latlongList = ArrayList()

        //if latlng list is not empty draw geofence on map
        //empty origional latlnglist, latlnglist from sharedpreference when removing geofence
//        if(checkForLastLngList()!!.size>0){
//            latlongList!!.addAll(checkForLastLngList()!!)
//            addMarkerForList(latlongList)
//            DrawGeofence()
//            tvRemoveGeofence.visibility = View.VISIBLE
//            tvAddGeofence.visibility = View.GONE
//        }
        ivLogout!!.setOnClickListener {
            //notificationManager!!.notify(1111, builder!!.build())

            val builder = AlertDialog.Builder(this)
                 builder.setTitle("Confirm Logout ?")
                 builder.setPositiveButton("Yes"){dialogInterface, which ->
                     auth!!.signOut()
                     finish()

                 }
                 builder.setNegativeButton("Cancel"){dialogINterface, which->
                     if(alertDialog!=null){
                         alertDialog!!.dismiss()
                     }
                 }

            alertDialog = builder.create()
            alertDialog!!.show()

        }


        tvAddGeofence.setOnClickListener {

            if(latlongList!!.size<=0 && mGeoFenceList!!.size<=0){
                Toast.makeText(this, "Mark Location to Draw  Geofence", Toast.LENGTH_SHORT).show()
            }else
            {
                DrawGeofence()
                tvRemoveGeofence.visibility = View.VISIBLE
                tvAddGeofence.visibility = View.GONE
            }
        }

        tvRemoveGeofence.setOnClickListener {
            removeGeofence()
            tvAddGeofence.visibility = View.VISIBLE
            tvRemoveGeofence.visibility = View.GONE

        }

        ivSetTime.setOnClickListener {
            val timePicker:TimePickerDialog
            val datePicker:DatePickerDialog
             c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val date = c.get(Calendar.DATE)
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)
            val second =c.get(Calendar.SECOND)

            if(latlongList!!.size<=0 && mGeoFenceList!!.size<=0){
                Toast.makeText(this, "Mark Location to Start Geofence", Toast.LENGTH_SHORT).show()
            }else
            {

                timePicker = TimePickerDialog(this, object:OnTimeSetListener{
                    override fun onTimeSet(p0: TimePicker?, hour: Int, minute: Int) {
                        //do something
                        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE),hour, minute)
                        setAlarmForRepeatingGeofencing(c, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE),hour, minute)
                    }

                },hour, minute, false)

                datePicker = DatePickerDialog(this, object :OnDateSetListener{
                    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, day: Int) {
                        c.set(year, month, day)
                        timePicker.show()
                    }

                }, year, month, date)

                datePicker.show()


           }
        }


    }

    private fun disablePowerOptization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()
            val packageName = packageName
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.action =
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }    }

    private fun initView() {
        ivLogout = findViewById(R.id.iv_logout)
        tvAddGeofence = findViewById(R.id.tv_add_geofence)
        tvRemoveGeofence = findViewById(R.id.tv_remove_geofence)
        ivSetTime = findViewById(R.id.iv_set_time)

    }


    private fun setAlarmForRepeatingGeofencing(
        c: Calendar,
        year: Int,
        month: Int,
        date: Int,
        hour: Int,
        minute: Int
    ) {
        //set alarm get
                 Log.i(TAG, "set alarm ${year}, ${month}, ${date}, ${hour}, ${minute}")

                Toast.makeText(this, "Starting geofencing please wait...", Toast.LENGTH_LONG).show()

                val mAlarmManger = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(this, Receiver::class.java)
                    .putExtra("Action", "AlarmStartGeoFence")
                     intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                      intent.putExtra("Triggered_Time", c.timeInMillis)

                val pendingIntent:PendingIntent

                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.S){
                    pendingIntent = PendingIntent.getBroadcast(this, 120, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

                }else{
                    pendingIntent = PendingIntent.getBroadcast(this, 120, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                }

        mAlarmManger.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, c.timeInMillis,
             pendingIntent
        )

       Toast.makeText(this, "geofencing scheduled from ${year}: ${month}: ${date}: ${hour}: ${minute}", Toast.LENGTH_LONG).show()

    }


    fun scheduleGeoFenceStop(s: String) {
        val mAlarmManger = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, Receiver::class.java).putExtra("Action", s)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("triggeredTime", c.timeInMillis)
        val pendingIntent:PendingIntent

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.S){
            pendingIntent = PendingIntent.getBroadcast(this, 120, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        }else{
            pendingIntent = PendingIntent.getBroadcast(this, 120, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        //val calendar = Calendar.getInstance()
        val hourToStop:Int
        val currentHourIn24Format: Int =c.get(Calendar.HOUR_OF_DAY) // return the hour in 24 hrs format (ranging from 0-23)
        if(currentHourIn24Format==23){
            hourToStop = 1
        }else if (currentHourIn24Format==24) {
          hourToStop = 2
        }else{
            hourToStop = currentHourIn24Format+2
        }

        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE),c.get(Calendar.HOUR), c.get(Calendar.MINUTE)+2)
        mAlarmManger.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, c.timeInMillis,
            pendingIntent
        )

        Toast.makeText(this, "geofencing will be stoped from ${c.get(Calendar.YEAR)}: ${c.get(Calendar.MONTH)}: ${c.get(Calendar.DATE)}: ${hourToStop}: ${c.get(Calendar.MINUTE)}", Toast.LENGTH_LONG).show()

    }

    private fun createGoogleApiClient() {
            mGoogleApiClient =  GoogleApiClient
                .Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener (this)
                .addApi(LocationServices.API)
                .build()

        }

        private fun initGMaps() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    var mLocationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        interval = (10 * 1000)
        fastestInterval = (1 * 1000)
    }



   fun checkStoragePermission(): Boolean {
       if (ActivityCompat.checkSelfPermission(
               this,
               Manifest.permission.ACCESS_FINE_LOCATION
           ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
               this,
               Manifest.permission.ACCESS_COARSE_LOCATION
           ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
               this,
               Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
           ) == PackageManager.PERMISSION_GRANTED
       ) {
           if(Build.VERSION.SDK_INT>=29){
               if (ActivityCompat.checkSelfPermission(
                       this,
                       Manifest.permission.ACCESS_BACKGROUND_LOCATION
                   ) == PackageManager.PERMISSION_GRANTED)
               {
                   if(Build.VERSION.SDK_INT>=33){
                       if(ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED){
                         return true
                       }else {
                           ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), PUSH_NOTIFICATION_CODE)
                           return false
                       }
                   }else{
                       return true
                   }
               }else {
                   ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),Background_PERMISSION_CODE )
                   return false
               }
           }else{
               return true
           }
       }else
       {
           ActivityCompat.requestPermissions(this,
               arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS),
               STORAGE_PERMISSION_CODE)
           return false
       }
       return false
   }

    fun checkPushNotificationpermission(): Boolean {
        if(Build.VERSION.SDK_INT>=33){
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED){
                return true
            }else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), PUSH_NOTIFICATION_CODE)
                return true
            }
        }
        return true
    }

    fun checkAccessBackgroundLocationPermission():Boolean{
        if(Build.VERSION.SDK_INT>=29){
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
            {
                checkPushNotificationpermission()

            }else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),Background_PERMISSION_CODE )
                return false
            }
        }
        return false
    }




    override fun onLocationChanged(p0: Location) {
        lastKnownLocation = p0
        mLatitute= p0.latitude
        mLongitude=p0.longitude
        writeActualLocationOnMap(p0)
    }

    fun removeGeofence(){
        if(checkStoragePermission()){
            val geofencingClient = LocationServices.getGeofencingClient(this)
            geofencingClient.removeGeofences(getGeofencingPendingIntent()!!)
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
        speditor.remove("latlnglist")
        speditor.apply()
        gMap!!.clear()
        latlongList!!.clear()
        mGeoFenceList!!.clear()

    }

    override fun onMapReady(p0: GoogleMap) {
        gMap = p0
        gMap!!.setOnMapClickListener(this)
        gMap!!.setOnMarkerClickListener(this)
    }

    private fun addMarkerForList(latlongList: ArrayList<LatLng>?) {
        for (i in latlongList!!){
            val markerOptions = MarkerOptions()
                .position(i)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .title("New Marker")

            if(gMap!=null){
                geofenceMarker = gMap!!.addMarker(markerOptions)
            }

            gMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(i, 14F))


        }

    }

    override fun onMapClick(latLng: LatLng) {
        addMarker(latLng)
    }

    private fun addMarker(latLng: LatLng) {
        val markerOptions = MarkerOptions()
            .position(latLng)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            .title("New Marker")

        if(gMap!=null){
            geofenceMarker = gMap!!.addMarker(markerOptions)
        }

        gMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14F))
        latlongList!!.add(latLng)


    }

        override fun onMarkerClick(p0: Marker): Boolean {
        return false
    }

        override fun onConnected(p0: Bundle?) {
            getLastKNownLOcation()
        }

        private fun getLastKNownLOcation() {
            if(checkStoragePermission()){
                lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
                if (lastKnownLocation!=null){
                    mLatitute = lastKnownLocation!!.latitude
                    mLongitude = lastKnownLocation!!.longitude
                    writeActualLocationOnMap(lastKnownLocation!!)
                    startLocationUpdate()
                }else{
                    startLocationUpdate()
                }
            }
        }

        private fun startLocationUpdate() {
             mLocationRequest = LocationRequest.create()
                 .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                 .setInterval(3 * 60 * 1000)
                 .setFastestInterval(30 * 1000)
            if(checkStoragePermission())
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)

        }

        override fun onConnectionSuspended(p0: Int) {
             Log.i(TAG, "Google Api connection suspended")
        }

        override fun onConnectionFailed(p0: ConnectionResult) {
            Toast.makeText(this,"This device doesn't support Googleplay Services",Toast.LENGTH_LONG).show()

        }

        override fun onStart() {
            super.onStart()
            mGoogleApiClient.connect()
        }

        override fun onStop() {
            super.onStop()
            mGoogleApiClient.disconnect()
        }

        private fun writeActualLocationOnMap(location: Location){
            markLocation(LatLng(location.latitude, location.longitude))
        }

        private fun markLocation(latLng: LatLng) {
              val markerOptions = MarkerOptions()
                        .position(latLng)
                  .title("Current Location")

             if(gMap!=null){
                   if(marker!=null)
                    marker!!.remove()
                    marker =  gMap!!.addMarker(markerOptions)
                    val cameraUpdate = CameraUpdateFactory.newLatLng(latLng)
                    gMap!!.animateCamera(cameraUpdate)
                 gMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14F))

             }

        }



        private fun createGeoFenceRequest(): GeofencingRequest {
            mGeoFenceList = ArrayList()
            for (coordinate in latlongList!!) {

                   geofence =   Geofence.Builder()
                        .setRequestId("My Geofence ID") // A string to identify this geofence
                        .setCircularRegion(
                            coordinate.latitude,
                            coordinate.longitude,
                            Geo_Fence_Radius
                        )
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setLoiteringDelay(11)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL or Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build()
                mGeoFenceList!!.add(geofence)

            }

            val builder = GeofencingRequest.Builder()

            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_EXIT or GeofencingRequest.INITIAL_TRIGGER_DWELL)

            builder.addGeofences(mGeoFenceList!!)

            return builder.build()
        }


        @SuppressLint("UnspecifiedImmutableFlag")
        private fun getGeofencingPendingIntent() : PendingIntent? {
            if (mGeofencePendingIntent != null) {
                return mGeofencePendingIntent
            }
            val intent = Intent(this, MyBroadcastReceiver::class.java).putExtra("Action", "GeoFence")

            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.S){
                mGeofencePendingIntent = PendingIntent.getBroadcast(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

            }else{
                mGeofencePendingIntent = PendingIntent.getBroadcast(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            }

            return mGeofencePendingIntent
        }

        fun DrawGeofence(){
            if(checkStoragePermission()){
                if(gMap!=null){
                    //startService(Intent(this, GeofenceService::class.java))
                    val geofenceRequest = createGeoFenceRequest()

                    geofencingClient.addGeofences(geofenceRequest, getGeofencingPendingIntent()!!)
                        .addOnSuccessListener {
                            initialise()
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
                            val error = getErrorString(it)
                            Toast.makeText(this, error,Toast.LENGTH_SHORT).show()
                        }

                }

            }

        }

    private fun initialise() {
            val  latLngBounds :LatLngBounds.Builder= LatLngBounds.builder()
            for (i in latlongList!!) {
                latLngBounds.include(i)
             }

        gMap!!.addCircle(
            CircleOptions()
                .strokeWidth(4f)
                .radius(1100.0)
                .center(latLngBounds.build().getCenter())
                .strokeColor(Color.parseColor("#D1C4E9"))
                .fillColor(Color.parseColor("#657C4DFF"))
        )

        gMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngBounds.build().center, 14F))
    }


    private fun getErrorString(e:Exception): String ?{
        if (e is ApiException) {
            val apiException = e as ApiException
            when (apiException.statusCode) {
                GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> {
                    return "GEOFENCE_NOT_AVAILABLE"
                }
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> {
                    return "GEOFENCE_TOO_MANY_PENDING_INTENTS"
                }

                GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> {
                    return "GEOFENCE_TOO_MANY_GEOFENCES"
                }
            }
        }

            return e.localizedMessage

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            STORAGE_PERMISSION_CODE ->{
                checkAccessBackgroundLocationPermission()
                if(grantResults.size>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()

                }else{
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }

            Background_PERMISSION_CODE ->{
                checkPushNotificationpermission()
                if(grantResults.size>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Access background location Permission granted", Toast.LENGTH_SHORT).show()

                }else{
                    Toast.makeText(this, "Access background location Denied", Toast.LENGTH_SHORT).show()
                }


            }

            PUSH_NOTIFICATION_CODE ->{
                if(grantResults.size>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Push notification Permission granted", Toast.LENGTH_SHORT).show()

                }else{
                    Toast.makeText(this, "Push notification location Denied", Toast.LENGTH_SHORT).show()
                }
            }

        }

    }
    override fun onResume() {
        super.onResume()
        Log.i(TAG, "${TAG} :onResume")

    }

    fun  checkForLastLngList(): ArrayList<LatLng>? {
        var arrayItems:ArrayList<LatLng>?= ArrayList()

        val serializedObject: String? = sp.getString("latlnglist", null)
        if (serializedObject != null) {
            val gson = Gson()
            val type: Type = object : TypeToken<List<LatLng?>?>() {}.type
            arrayItems = gson.fromJson(serializedObject, type)
        }
        return arrayItems
    }


    class Receiver :BroadcastReceiver(){
        var activityMain: MainActivity? = null
        override fun onReceive(context: Context?, intent: Intent?) {

            if(intent!!.action.equals("AlarmStartGeoFence")){
                context!!.startService(
                    Intent(context, NotificationHelper::class.java).putExtra(
                        "desc",
                        "Receiver: onReceive!AlarmStartGeoFence"
                    )
                )

                  activityMain!!.DrawGeofence()
                  activityMain!!.scheduleGeoFenceStop("AlarmStopGeoFence")
//
//                tvRemoveGeofence.visibility = View.VISIBLE
//                tvAddGeofence.visibility = View.GONE
//                ivSetTime.setImageDrawable(getDrawable(R.drawable.ic_baseline_access_alarm_24))

            }else if(intent.action.equals("AlarmStopGeoFence")){
                context!!.startService(
                    Intent(context, NotificationHelper::class.java).putExtra(
                        "desc",
                        "Receiver: onReceive!AlarmStopGeoFence"
                    )
                )
                   activityMain!!.removeGeofence()
//                tvRemoveGeofence.visibility = View.GONE
//                tvAddGeofence.visibility = View.VISIBLE
//
//                ivSetTime.setImageDrawable(getDrawable(R.drawable.ic_baseline_alarm_off_24))

            }
        }

        fun setMainActivityHandler(main: MainActivity) {
            activityMain = main
        }

    }


}





