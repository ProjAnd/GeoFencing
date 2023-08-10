package com.example.applicationgeofence;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyBroadcastReceiver extends BroadcastReceiver {
    private final String TAG = "MyBroadcastReceiver";
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databasereference;


    private SharedPreferences sp;
    private String currentUser;
    private Context ctx;
    private String action;


    @Override
    public void onReceive(Context context, Intent intent) {
        ctx = context;
        action = intent.getExtras().getString("Action", "");
        int Triggered_Time = intent.getExtras().getInt("Triggered_Time", 0);
        int triggeredTime = intent.getExtras().getInt("triggeredTime", 0);
        sp = context.getSharedPreferences("myPrefs", MODE_PRIVATE);
        if(action.equals("AlarmStartGeoFence")){
            context.sendBroadcast(new Intent("AlarmStartGeoFence"));
//            AlarmManager mAlarmManger = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//            Intent intent1 =new  Intent(context, MainActivity.Receiver.class)
//                    .putExtra("Action", "AlarmStartGeoFence");
//
//            PendingIntent pendingIntent;
//
//            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.S){
//                pendingIntent = PendingIntent.getBroadcast(context, 120, intent1, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
//
//            }else{
//                pendingIntent = PendingIntent.getBroadcast(context, 120, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
//            }
//
//            mAlarmManger.setExactAndAllowWhileIdle(
//                    AlarmManager.RTC_WAKEUP, Triggered_Time,
//                    pendingIntent
//            );
        }else if(action.equals("AlarmStopGeoFence")){
            context.sendBroadcast(new Intent("AlarmStopGeoFence"));
//            AlarmManager mAlarmManger = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//            Intent intent1 =new  Intent(context, MainActivity.Receiver.class).putExtra("Action", "AlarmStopGeoFence");
//
//            PendingIntent pendingIntent;
//
//            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.S){
//                pendingIntent = PendingIntent.getBroadcast(context, 120, intent1, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
//
//            }else{
//                pendingIntent = PendingIntent.getBroadcast(context, 120, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
//            }
//
//            mAlarmManger.setExactAndAllowWhileIdle(
//                    AlarmManager.RTC_WAKEUP, triggeredTime,
//                    pendingIntent
//            );
        }else {
            GeofenceIntentService.enqueueWork(context, intent);
            GeofencingEvent geoFenceEvent = GeofencingEvent.fromIntent(intent);
            SharedPreferences prefs = context.getSharedPreferences("myPrefs",
                    MODE_PRIVATE);
            currentUser = prefs.getString("userEMail", "");


            firebaseDatabase = FirebaseDatabase.getInstance();
            databasereference = firebaseDatabase.getReference("UserInfo");


            if(geoFenceEvent.hasError()){
                Toast.makeText(context, "OnReceive : Error while receiving Geofence Event", Toast.LENGTH_SHORT).show();
            }

            int transitionType = geoFenceEvent.getGeofenceTransition();

            switch(transitionType){
                case Geofence.GEOFENCE_TRANSITION_DWELL:
                    Log.i(TAG, "GEOFENCE_TRANSITION_DWELL");
                    break;

                case Geofence.GEOFENCE_TRANSITION_ENTER:
                    Log.i(TAG, "GEOFENCE_TRANSITION_ENTER");

                    break;

                case Geofence.GEOFENCE_TRANSITION_EXIT:
                    Log.i(TAG, "GEOFENCE_TRANSITION_EXIT");
                    context.startService(new Intent(context, NotificationHelper.class).putExtra("desc", "User Viaolation triggered !"));
                    senDataToFb(geoFenceEvent.getTriggeringLocation());

                    break;
            }
        }

    }


    void senDataToFb(Location triggeringLocation){
        UserInfo userInfo = new UserInfo(currentUser, triggeringLocation.getLatitude(), triggeringLocation.getLongitude());
        databasereference = databasereference.child("User Details");

        databasereference.push().setValue(userInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(ctx, "User Data Added to Firebase Database", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ctx, "Error while Saving Data To firebase Database", Toast.LENGTH_SHORT).show();
            }
        });

    }


}
