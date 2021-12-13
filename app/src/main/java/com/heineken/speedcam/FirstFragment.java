package com.heineken.speedcam;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.heineken.speedcam.databinding.FragmentFirstBinding;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


import static androidx.core.content.ContextCompat.getSystemService;

public class FirstFragment extends Fragment {
    private static final int MODE_MULTI_PROCESS = 4;
    private String MAP_ID = "1aPQbHgj6-sXbbI4K5MDuRkK7Oe0Sr5U4&ll=50.923479396995546%2C30.177172773963747&z";
    private String PREF_RUNNING = "service_status_preference";
    private FragmentFirstBinding binding;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState


    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //  binding.ServiceStatus.setText(String.valueOf(MainActivity.sharedPreferences.getBoolean(PREF_RUNNING, false)));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.ServiceStatus.setText(MyService.isSericeRunning() ? "Running" : "Not running");
        } else {
            binding.ServiceStatus.setText(getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_MULTI_PROCESS).getString(PREF_RUNNING, ""));
        }
        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  NavHostFragment.findNavController(FirstFragment.this)
                //           .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
        binding.UpdateRecords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DownloadKML url = new DownloadKML(getContext());
                try {
                    url.execute("https://www.google.com/maps/d/u/0/kml?mid=" + MAP_ID + "&forcekml=1").get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // String str_result= new RunInBackGround().execute().get();
                XMLParser read = new XMLParser(getContext());
                myDbAdapter adapter = new myDbAdapter(getContext());
                try {
                    int i = 0;
                    Vector camArray = read.readXML();
                    binding.RecordsCount.setText(String.valueOf(camArray.size()));
                    adapter.deleteDBase();
                    while (i < camArray.size()) {
                        adapter.addCamera(String.valueOf(camArray.get(i)), Float.parseFloat(String.valueOf(camArray.get(1 + i))), Float.parseFloat(String.valueOf(camArray.get(2 + i))));
                        i += 3;
                    }
                    //   adapter.addUser( "new", 3f,4f);
                    // String data=adapter.getData();
                    //  Log.d("Out", data);


                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });

        binding.ServiceStart.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                // SharedPreferences preferences = getActivity().getSharedPreferences(PREF_RUNNING, 0);
                // binding.ServiceStatus.setText(String.valueOf(MainActivity.sharedPreferences.getBoolean(PREF_RUNNING,false)));

                //   binding.ServiceStatus.setText(gps.isRunning ? "Running" : "Not running");
                // Log.d( "Service running:", String.valueOf((gps.isRunning)));

                // switch (getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_MULTI_PROCESS).getString(PREF_RUNNING, "")) {

                //       case "started":
                //  if (getActivity().stopService(new Intent(getActivity(), MyService.class)))
                //      binding.ServiceStatus.setText("stopped");
                //     break;
                //     case "stopped":

                //  default:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (!MyService.isSericeRunning()) {
                        getActivity().startService(new Intent(getActivity(), MyService.class));
                    } else {
                        getActivity().stopService(new Intent(getActivity(), MyService.class));

                    }
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            binding.ServiceStatus.setText(MyService.isSericeRunning() ? "Running" : "Not running");


                        }
                    }, 2000);
                } else {
                   Log.d("Activity", getContext().getSharedPreferences(getActivity().getPackageName(), Context.MODE_MULTI_PROCESS).getString(PREF_RUNNING, ""));
                    if (getContext().getSharedPreferences(getActivity().getPackageName(), Context.MODE_MULTI_PROCESS).getString(PREF_RUNNING, "").contentEquals("Running")) {
                        getActivity().stopService(new Intent(getActivity(), MyService.class));
                        getContext().getSharedPreferences(getActivity().getPackageName(), Context.MODE_MULTI_PROCESS).edit().putString(PREF_RUNNING, "Not running");
                    } else {
                        getActivity().startService(new Intent(getActivity(), MyService.class));
                    //    getContext().getSharedPreferences(getActivity().getPackageName(), Context.MODE_MULTI_PROCESS).edit().putString(PREF_RUNNING, "Not running");
                    }
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            binding.ServiceStatus.setText(getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_MULTI_PROCESS).getString(PREF_RUNNING, ""));
                        }
                    }, 2000);

                }

            }


                     //   binding.ServiceStatus.setText("started");


                        // };

                        //  {
                        //  if (getActivity().startService(new Intent(getContext(), gps.class)) != null) ;

                        //  binding.ServiceStatus.setText(String.valueOf(MainActivity.sharedPreferences.getString(PREF_RUNNING,"")));
                        //   } else {
                        // if (getActivity().stopService(new Intent(getContext(), gps.class)) != false) ;
                        //   binding.ServiceStatus.setText(String.valueOf(MainActivity.sharedPreferences.getBoolean(PREF_RUNNING, false)));




        });



}

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
     //  binding.ServiceStatus.setText(String.valueOf(MainActivity.sharedPreferences.getString(PREF_RUNNING, "")));
        //   if (MainActivity.sharedPreferences.getBoolean(PREF_RUNNING, true))
        //      Log.e ("Test","true");
        //      else
        //      Log.e ("Test","false");
        // }
        //Log.e ("Test",String.valueOf(getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_MULTI_PROCESS).getString(PREF_RUNNING, "")));
        //Log.e("Test", String.valueOf(MainActivity.sharedPreferences.getString(PREF_RUNNING,"")));
    }
}




