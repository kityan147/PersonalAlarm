package com.example.kit.personalalarm;

/**
 * Created by Wｉｌｌｉａｍ　Ｌｏ on 2017/02/02.
 */
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

import android.util.Log;
import android.widget.Toast;

public class GPS extends Service implements LocationListener {

    private final Context mContext;

    boolean isGPSon = false;
    boolean isNetworkon = false;
    boolean isgetLocation = false;

    Location location;
    double lat;
    double lon;

    private static final int dis_to_up = 10;
    private static final int time_to_up = 60000; //1min
    protected LocationManager locationmanager;

    public GPS(Context mContext) {
        this.mContext = mContext;
        getLocation();
    }

    private Location getLocation() {
        try {
            locationmanager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            isGPSon = locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkon = locationmanager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isNetworkon && !isGPSon) {
                Toast.makeText(mContext, "No Network Service Provided", Toast.LENGTH_SHORT).show();
                showSettingsAlert();
            } else {
                isgetLocation = true;
                if (isNetworkon) {
                    Toast.makeText(mContext, "Network", Toast.LENGTH_SHORT).show();

                    try {
                        locationmanager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                time_to_up,
                                dis_to_up, this);
                        Log.d("Network", "Network");
                        if (locationmanager != null) {
                            location = locationmanager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        }

                        if (location != null) {
                            lat = location.getLatitude();
                            lon = location.getLongitude();
                        }
                    } catch (SecurityException e) {

                    }
                }

                // if GPS Enabled get lat/long using GPS Services
                if (isGPSon) {
                    Toast.makeText(mContext, "GPS", Toast.LENGTH_SHORT).show();
                    if (location == null) {
                        try {
                            locationmanager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    time_to_up,
                                    dis_to_up, this);
                            Log.d("GPS Enabled", "GPS Enabled");
                            if (locationmanager != null) {
                                location = locationmanager
                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (location != null) {
                                    lat = location.getLatitude();
                                    lon = location.getLongitude();
                                }
                            }
                        } catch (SecurityException e) {

                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("GPS Not Enabled");
        alertDialog.setMessage("Do you wants to turn On GPS");

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    public double getLongitude() {
        if (location != null) {
            lon = location.getLongitude();
        }
        return lon;
    }

    public double getLatitude() {
        if (location != null) {
            lat = location.getLatitude();
        }
        return lat;
    }


    public boolean isgetLocation() {
        return this.isgetLocation;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
