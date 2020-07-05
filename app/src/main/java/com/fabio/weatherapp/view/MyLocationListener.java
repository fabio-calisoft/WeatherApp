package com.fabio.weatherapp.view;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


import java.io.IOException;
import java.util.List;
import java.util.Locale;


class MyLocationListener implements LocationListener {

    Context context;

    public MyLocationListener(Context mContext) {
        context = mContext;
    }


    @Override
    public void onLocationChanged(Location loc) {
        Log.d("fdl", "Location changed: Lat: " + loc.getLatitude() + " Lng: "
                + loc.getLongitude());


        String longitude = "Longitude: " + loc.getLongitude();
        Log.v("fdl", longitude);
        String latitude = "Latitude: " + loc.getLatitude();
        Log.v("fdl", latitude);

        /*------- To get city name from coordinates -------- */
        String cityName = null;
        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(loc.getLatitude(),
                    loc.getLongitude(), 1);
            if (addresses.size() > 0) {
                System.out.println(addresses.get(0).getLocality());
                cityName = addresses.get(0).getLocality();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onProviderDisabled(String provider) {}


    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
}