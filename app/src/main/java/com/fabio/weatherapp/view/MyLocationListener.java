package com.fabio.weatherapp.view;

import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.fabio.weatherapp.R;

import static android.content.Context.MODE_PRIVATE;


class MyLocationListener implements LocationListener {

    AppCompatActivity context;

    public MyLocationListener(AppCompatActivity mContext) {
        context = mContext;
    }


    @Override
    public void onLocationChanged(Location loc) {
        Log.d("fdl", "Location changed: Lat: " + loc.getLatitude() + " Lng: "
                + loc.getLongitude());
        // save last location in SharedPreferences
        SharedPreferences sharedPreferences = context.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("lat", (float) loc.getLatitude());
        editor.putFloat("long", (float) loc.getLongitude());
        editor.apply();
        context.setContentView(R.layout.activity_main);
    }

    @Override
    public void onProviderDisabled(String provider) {
    }


    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
}
