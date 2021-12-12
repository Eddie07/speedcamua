package com.heineken.speedcam;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

class DownloadKML extends AsyncTask<String, String, Boolean> {

    private final Context mContext;


    public DownloadKML(final Context context) {
        mContext = context;
    }
    private int counts;
    /**
     * Before starting background thread
     * */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        System.out.println("Starting download");
    }

    /**
     * Downloading file in background thread
     * */
    @Override
    protected Boolean doInBackground(String... f_url) {
        int count;
        try {
            String root = Environment.getExternalStorageDirectory().toString();

            System.out.println("Downloading");
            URL url = new URL(f_url[0]);

            URLConnection connection = url.openConnection();
            connection.connect();
            // getting file length
            int lenghtOfFile = connection.getContentLength();

            // input stream to read file - with 8k buffer
            InputStream input = new BufferedInputStream(url.openStream(), 8192);

            // Output stream to write file
            OutputStream output = new FileOutputStream(mContext.getFilesDir() + "/maps.xml");
            byte data[] = new byte[1024];

            long total = 0;
            while ((count = input.read(data)) != -1) {
                total += count;

                // writing data to file
                output.write(data, 0, count);

            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();



        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
            return false;

        }

        return true;
    }



    /**
     * After completing background task
     * **/
    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (result) System.out.println("Downloaded");
        else System.out.println("Failed");

    }

}
