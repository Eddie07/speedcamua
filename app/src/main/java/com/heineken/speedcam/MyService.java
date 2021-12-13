package com.heineken.speedcam;

import android.app.*;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Vector;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MyService extends Service {
    private static final float LOCATION_DISTANCE = 0;
    private static final String TAG = "GPS";
    private LocationManager mLocationManager = null;
    private NotificationChannel channel;
    private NotificationManager notificationManager;
    private NotificationManagerCompat mBuilder;
    private String PREF_RUNNING = "service_status_preference";
    private String PREF_LOCATION_UPDATE_INTERVAL = "location_update_interval_preference";
    private String CHANNEL_ID = "gps_message_01";
    private String CHANNEL_NAME_SERVICE = "GPS tracking service";
    private String CHANNEL_ID_SERVICE = "gps_service_01";
    private String CHANNEL_NAME = "Warning message";
    private static boolean isRunning=false;
    private static boolean nextCameraInFocus=false;
    private static boolean showOnce=false;
    private static double oldDistance=0.0;
    private static long oldTime=0l;


    public static Boolean isSericeRunning () {

        return isRunning;
    }


    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;


        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }


        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged: " + location);
            myDbAdapter adapter=new myDbAdapter(getApplicationContext());
            Vector data=adapter.getData();

            for (int i=0; i<data.size();i+=3 ){
                double distance= Haversine.distance(Double.parseDouble(String.valueOf(data.get(2+i))), Double.parseDouble(String.valueOf(data.get(1+i))),
                        Double.parseDouble(String.valueOf(location.getLatitude())),Double.parseDouble(String.valueOf(location.getLongitude())));
                if (distance < 0.7 &&  !nextCameraInFocus) {
                        mLocationManager.requestLocationUpdates(
                                location.getProvider(), (2000), LOCATION_DISTANCE,
                                new LocationListener(location.getProvider()));
                    nextCameraInFocus = true;
                    break;

                }
                if ((distance > 1.0 ) && (nextCameraInFocus = true)) {
                    mLocationManager.requestLocationUpdates(
                            location.getProvider(), (getSharedPreferences("com.heineken.speedcam", Context.MODE_MULTI_PROCESS).getInt(PREF_LOCATION_UPDATE_INTERVAL, 40000)), LOCATION_DISTANCE,
                            new LocationListener(location.getProvider()));
                    nextCameraInFocus=false;
                    showOnce=false;
                }
                if (distance < 0.4 && !showOnce ) {
                    showNotification( (getString(R.string.notification_message) +" " + String.format("%.0f", distance*1000) + "m. "+ String.valueOf(data.get(i))));
                    showOnce=true;
                    break;
                }
               // if (nextCameraInFocus) {
               //     showNotification( getString(R.string.notification_message) +" " + String.format("%.0f", distance*100) + "m. "+ String.valueOf(data.get(i)));
               //     Log.e(TAG, "onLocationChanged: " + nextCameraInFocus);
               //     break; }


               /* if (distance <0.35 && distance > oldDistance && (location.getTime()-oldTime>10000)) {
                   showNotification( getString(R.string.notification_message) +" " + String.format("%.0f", distance*100) + "m. "+ String.valueOf(data.get(i)));
                   oldTime=location.getTime();
                   oldDistance=distance;
                   break; }

                if (distance <0.35  && location.getTime()-oldTime>=20000) {
                    oldTime = 0l;
                    oldDistance = 0.0;/*
                }
                mLocationManager.requestLocationUpdates(
                        location.getProvider(), (getApplicationContext().getSharedPreferences("com.heineken.speedcam", Context.MODE_MULTI_PROCESS).getInt(PREF_LOCATION_UPDATE_INTERVAL, 10000)), LOCATION_DISTANCE,
                        mLocationListeners[0]);
                        */


            }

        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");

        myDbAdapter adapter=new myDbAdapter(getApplicationContext());
        /* exit when database empty */
        Vector data=adapter.getData();

        if ((data.size() < 1)||(this.getSharedPreferences("com.heineken.speedcam", Context.MODE_MULTI_PROCESS).getString(PREF_RUNNING, "")=="Running"))
        { stopForeground(true);
            stopSelf();}

        /* message that service is started */
        isRunning =true;
        this.getSharedPreferences("com.heineken.speedcam", Context.MODE_MULTI_PROCESS).edit().putString(PREF_RUNNING, "Running").commit();


        if (Build.VERSION.SDK_INT >= 26) {

        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_SERVICE)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setPriority(PRIORITY_MIN)
                .build();
        startForeground(101, notification);
        TextView t = new TextView(this);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        super.onCreate();

        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, (this.getSharedPreferences("com.heineken.speedcam", Context.MODE_MULTI_PROCESS).getInt(PREF_LOCATION_UPDATE_INTERVAL, 40000)), LOCATION_DISTANCE,
                    mLocationListeners[0]);

        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            try {  mLocationManager.requestLocationUpdates(
                             LocationManager.NETWORK_PROVIDER, (this.getSharedPreferences("com.heineken.speedcam", Context.MODE_MULTI_PROCESS).getInt(PREF_LOCATION_UPDATE_INTERVAL, 40000)), LOCATION_DISTANCE,
                              mLocationListeners[1]);
                  } catch (java.lang.SecurityException ex2) {
                      Log.i(TAG, "fail to request location update, ignore", ex);
                  } catch (IllegalArgumentException ex2) {
                      Log.d(TAG, "network provider does not exist, " + ex.getMessage());
                 }
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        /* message that service is stopped */
        isRunning=false;
        this.getSharedPreferences("com.heineken.speedcam", Context.MODE_MULTI_PROCESS).edit().putString(PREF_RUNNING, "Not running").commit();

        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public void showNotification(String loc) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_action_cam)
                        .setContentTitle(getString(R.string.notification_title))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentText(loc);
       // mBuilder.setPriority(NotificationCompat.PRIORITY_MIN);

        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(this);
        mNotificationManager.notify(0, mBuilder.build());
        //    Notification notification = mBuilder.build();
        //  notification.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        // mNotificationManager.notify(1, notification);


    }
}