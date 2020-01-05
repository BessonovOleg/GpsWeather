package com.example.gpsweather.location;

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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import com.example.myapplication2.R;


public class LocationService extends Service implements LocationListener {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    Context mContext;
    boolean isGPSEnabled = false;
    // flag for GPS status
    boolean canGetLocation = false;
    Location location; // location
    double latitude; // latitude
    double longitude; // longitude
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 500 * 10 * 1; // 5 seconds
    // Declaring a Location Manager
    protected LocationManager locationManager;
    public LocationService(Context context) {
        mContext = context;
        locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        getLocation();
    }
    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
     }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
    @Override
    public void onProviderEnabled(String provider) {
    }
    @Override
    public void onProviderDisabled(String provider) {
    }

    public Location getLocation() {
        try {
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            this.canGetLocation = true;
                    if (isGPSEnabled) {
                        if (location == null) {
                            if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                this.canGetLocation = false;
                            } else {
                                locationManager.requestLocationUpdates(
                                        LocationManager.GPS_PROVIDER,
                                        MIN_TIME_BW_UPDATES,
                                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                                if (locationManager != null) {
                                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                    if (location != null) {
                                        latitude = location.getLatitude();
                                        longitude = location.getLongitude();
                                    }
                                }
                            }
                        }
                    }else {
                        this.canGetLocation = false;
                    }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        return latitude;
    }
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        return longitude;
    }
    public boolean canGetLocation() {
        return this.canGetLocation;
    }
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle(mContext.getResources().getString(R.string.gpsSettingCaption));
        alertDialog.setMessage(mContext.getResources().getString(R.string.gpsSettingText));
        alertDialog.setPositiveButton(mContext.getResources().getString(R.string.gpsSettingBtnSettingText), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });
        alertDialog.setNegativeButton(mContext.getResources().getString(R.string.gpsSettingBtnCancelText), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }
}