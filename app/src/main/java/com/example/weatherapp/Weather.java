package com.example.weatherapp;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Weather extends AsyncTask<String,Void,String> {

    @Override
    protected String doInBackground(String... address) {

        try {
            URL url = new URL(address[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //nawiazanie polaczenia z adresem
            connection.connect();

            //pobranie danych z URL
            InputStream is = connection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);

            int data = isr.read();
            String content = "";
            char ch;
            while (data > 0){
                ch = (char) data;
                content = content + ch;
                data = isr.read();
            }
            Log.i("Content",content);
            return content;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
