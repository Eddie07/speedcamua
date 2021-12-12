package com.heineken.speedcam;

import android.app.*;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import android.content.res.Resources;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Vector;

import static androidx.core.app.NotificationCompat.PRIORITY_DEFAULT;
import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MyService extends Service {
    private static final int LOCATION_INTERVAL = 1000 * 10;
    private static final float LOCATION_DISTANCE = 0;
    private static final String TAG = "GPS";
    private LocationManager mLocationManager = null;
    private NotificationChannel channel;
    private NotificationManager notificationManager;
    private NotificationManagerCompat mBuilder;
    private String PREF_RUNNING = "service_status";
    private String CHANNEL_ID = "Warning";
    private String CHANNEL_NAME_SERVICE = "GPS tracking service";
    private String CHANNEL_ID_SERVICE = "GPS service";
    private String CHANNEL_NAME = "Warning message";
    private static Boolean isRunning=false;
    private static Double oldDistance=0.0;
    private static Long oldTime=0l;


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
                Double distance= Haversine.distance(Double.parseDouble(String.valueOf(data.get(2+i))), Double.parseDouble(String.valueOf(data.get(1+i))),
                        Double.parseDouble(String.valueOf(location.getLatitude())),Double.parseDouble(String.valueOf(location.getLongitude())));
                if (distance <3.2)  Log.e(TAG, "Distance: " + distance + " "+ String.valueOf(data.get(i)) + location.getTime());

               if (distance <3.2 && distance > oldDistance && (location.getTime()-oldTime>10000)) {
                   showNotification( distance + " " + String.valueOf(data.get(i)));
                   oldTime=location.getTime();
                   oldDistance=distance;

                   break; }
                if (distance <3.2  && location.getTime()-oldTime>20000) {
                    oldTime = 0l;
                    oldDistance = 0.0;
                }

            }


            //  showNotification(location.toString());

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
        Log.d ("Debug text" , this.getSharedPreferences("com.heineken.speedcam", Context.MODE_MULTI_PROCESS).getString(PREF_RUNNING, ""));
        if (data.size() < 1)  stopSelf();
        if (this.getSharedPreferences("com.heineken.speedcam", Context.MODE_MULTI_PROCESS).getString(PREF_RUNNING, "")=="Running") stopForeground(true);
      // stopSelf();
       // stopSelf();


        /* message that service is started */
        isRunning =true;
        this.getSharedPreferences("com.heineken.speedcam", Context.MODE_MULTI_PROCESS).edit().putString(PREF_RUNNING, "Running").commit();
        //super.onStartCommand(intent, flags, startId);

        //     stopSelf();
        //  startServiceOreoCondition();

        //shagetBoolean(PREF_RUNNING,false);
        //   MainActivity.sharedPreferences.edit().putBoolean (PREF_RUNNING, true).apply();

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_SERVICE, CHANNEL_NAME_SERVICE, NotificationManager.IMPORTANCE_NONE);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
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


      //  try {
      //      mLocationManager.requestLocationUpdates(
      //              LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
     //               mLocationListeners[1]);
      //  } catch (java.lang.SecurityException ex) {
      //      Log.i(TAG, "fail to request location update, ignore", ex);
      //  } catch (IllegalArgumentException ex) {
      //      Log.d(TAG, "network provider does not exist, " + ex.getMessage());
     //   }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);

        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
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

        //MainActivity.sharedPreferences.edit().putBoolean (PREF_RUNNING, false).apply();
        // MainActivity.sharedPreferences.getBoolean(PREF_RUNNING,false);
        //  MainActivity.sharedPreferences.edit().putBoolean (PREF_RUNNING, true).apply();
        //   isRunning = false;
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

    private void startServiceOreoCondition() {

    }


    public void showNotification(String loc) {

        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/raw/alarm");
        /* Oreo+ workarround */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_HIGH;

            channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            //  channel.setDescription(description);
            channel.setSound(soundUri, new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                    .build());
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        /* Oreo+ workarround */
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(com.heineken.speedcam.R.drawable.ic_launcher_background)
                        .setContentTitle("Hello, attention!")
                        .setContentText(loc)
                        .setSound(soundUri, AudioManager.STREAM_MUSIC)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(this);
        mNotificationManager.notify(0, mBuilder.build());
        //    Notification notification = mBuilder.build();
        //  notification.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        // mNotificationManager.notify(1, notification);


    }
}