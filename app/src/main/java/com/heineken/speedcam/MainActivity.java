package com.heineken.speedcam;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.heineken.speedcam.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.security.Provider;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private String MAP_ID="1aPQbHgj6-sXbbI4K5MDuRkK7Oe0Sr5U4&ll=50.923479396995546%2C30.177172773963747&z";
    private String PREF_RUNNING = "service_status";
    myDbAdapter helper=new myDbAdapter(this);
    public static SharedPreferences sharedPreferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    //  sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        //sharedPreferences.edit().putBoolean (PREF_RUNNING, false).commit();
     //   sharedPreferences.edit().putString (PREF_RUNNING, "stopped").apply();

       TextView ServiceStatus = findViewById(R.id.ServiceStatus);
       // ServiceStatus.setText(String.valueOf(sharedPreferences.getBoolean(PREF_RUNNING,false)));
       //binding.ServiceStatus.setText(String.valueOf(preferences.getBoolean(PREF_RUNNING,false)));
       // myDbAdapter helper;

        //check permissions
      //  if (!checkIfAlreadyhavePermission())
            requestForSpecificPermission();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

           //



        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showNotification();
                myDbAdapter adapter=new myDbAdapter(getApplication());
            //    adapter.deleteDBase();

              //  adapter.addCamera( "new", 3f,4f);
                Vector data=adapter.getData();
               // Log.d("Data",data);
              //  DownloadKML url = new DownloadKML(getApplicationContext());
               // url.execute("https://www.google.com/maps/d/u/0/kml?mid="+MAP_ID+"&forcekml=1");

            }


        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
    private boolean checkIfAlreadyhavePermission() {
        int result = (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) & ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION));
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }


    private void requestForSpecificPermission() {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.FOREGROUND_SERVICE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
        if  ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)) finish();
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!isGPSEnabled(this)) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

    }
    public boolean isGPSEnabled (Context mContext){
        LocationManager locationManager = (LocationManager)
                mContext.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

 /*   public void addUser( String name, Float x, Float y)
    {


      long id= helper.insertData(name,x,y);
        helper.getData();
      if(id<=0)
      {   Log.d ("SQL", "Error");

       } else
      {
           Log.d ("SQL", "OK");
       }
      Log.d ("SQL",helper.getData());
    } */
}


