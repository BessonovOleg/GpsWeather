package com.example.gpsweather;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.gpsweather.network.NetworkService;
import com.example.gpsweather.network.Weather;
import com.example.gpsweather.network.WeatherApi;
import com.example.myapplication2.R;
import com.example.gpsweather.dao.AppDatabase;
import com.example.gpsweather.dao.DataBaseFactory;
import com.example.gpsweather.dao.HistoryRecord;
import com.example.gpsweather.dao.HistoryRecordDao;
import com.example.gpsweather.history.HistoryActivity;
import com.example.gpsweather.location.LocationService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button btnMapMode;
    private Button btnMyLocation;
    private Button btnWeatherMyLocation;
    private Button btnFreeMarker;
    private Button btnWeatherFM;
    private Button btnHistory;
    private WeatherApi weatherApi;
    private Location myLocation;
    private MarkerOptions myLocationMarkerOptions;
    private Marker myLocationMarker;
    private boolean isWaitClickToMap;

    private Marker freeMarker;
    private static MarkerOptions freeMarkerInitOptions;

    private AppDatabase db;
    private HistoryRecordDao historyRecordDao;

    private static final CharSequence[] MAP_TYPE_ITEMS =
            {"Карта", "Спутник", "Контур", "Гибрид"};

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 2;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {
            return;
        }

        Bundle argument = data.getExtras();
        final HistoryRecord historyRecord;
        historyRecord = (HistoryRecord) argument.getSerializable(HistoryRecord.class.getSimpleName());

        if (mMap != null) {
            LatLng newFreeMarkerLocation = new LatLng(historyRecord.lat, historyRecord.lon);
            freeMarker.setPosition(newFreeMarkerLocation);
            freeMarker.setVisible(true);
            isWaitClickToMap = false;
            mMap.moveCamera(CameraUpdateFactory.newLatLng(newFreeMarkerLocation));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        DataBaseFactory dataBaseFactory = new DataBaseFactory(this);
        db = dataBaseFactory.getDataBase();
        historyRecordDao = db.historyRecordDao();

        isWaitClickToMap = false;

        myLocationMarkerOptions = new MarkerOptions();
        myLocationMarkerOptions.title("Я");
        myLocationMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        myLocationMarkerOptions.position(new LatLng(0,0));
        myLocationMarkerOptions.visible(false);

        freeMarkerInitOptions = new MarkerOptions();
        freeMarkerInitOptions.position(new LatLng(0,0));
        freeMarkerInitOptions.visible(false);

        //For only portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION
            );
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            );
        }

        //Weather api init
            weatherApi = NetworkService.getInstance().getWeatherApi();
        //


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        btnFreeMarker = (Button)findViewById(R.id.btnFreeMarker);
        btnFreeMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (freeMarker != null){
                    freeMarker.setVisible(false);
                    isWaitClickToMap = true;
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.message_select_location),Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnHistory = (Button)findViewById(R.id.btnHistory);
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, HistoryActivity.class);
                startActivityForResult(intent,1);
            }
        });

        btnMapMode = (Button)findViewById(R.id.btnMapMode);
        btnMapMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mMap != null){
                    showMapTypeSelectorDialog();
                }
            }
        });

        btnMyLocation = (Button)findViewById(R.id.btnMyLocation);
        btnMyLocation.setOnClickListener(new View.OnClickListener() {
            final Context context = MapsActivity.this;
            @Override
            public void onClick(View v) {
                LocationService finder;
                double longitude = 0.0, latitude = 0.0;
                finder = new LocationService(context);
                if (finder.canGetLocation()) {
                    boolean isLocationaFind = false;
                    Toast.makeText(getBaseContext(),getResources().getString(R.string.message_wait_gps_location),Toast.LENGTH_SHORT).show();

                    while (!isLocationaFind) {
                        myLocation = finder.getLocation();
                        latitude = finder.getLatitude();
                        longitude = finder.getLongitude();
                        if (latitude != 0 && longitude != 0){
                            isLocationaFind = true;
                        }
                    }

                    if(latitude == 0 && longitude == 0){
                        Toast.makeText(getBaseContext(),getResources().getString(R.string.message_try_get_location_later),Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(mMap != null){
                        LatLng myCursorLoacion = new LatLng(latitude,longitude);
                        myLocationMarker.setPosition(myCursorLoacion);
                        myLocationMarker.setVisible(true);
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(myCursorLoacion));
                    }
                } else {
                    Toast.makeText(getBaseContext(),getResources().getString(R.string.message_gps_disable),Toast.LENGTH_SHORT).show();
                    //finder.showSettingsAlert();
                }
            }
        });

        btnWeatherMyLocation = (Button)findViewById(R.id.btnWeatherMyLocation);
        btnWeatherMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!myLocationMarker.isVisible()){
                    Toast.makeText(getBaseContext(),getResources().getString(R.string.message_dont_find_my_location),Toast.LENGTH_SHORT).show();
                    return;
                }
                LatLng latLng = myLocationMarker.getPosition();
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                try {
                    getWeatherByLocation(myLocation);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });


        btnWeatherFM = (Button)findViewById(R.id.btnWeatherFM);
        btnWeatherFM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(freeMarker != null){
                    if(freeMarker.isVisible()){
                        Location loc = new Location("");
                        loc.setLatitude(freeMarker.getPosition().latitude);
                        loc.setLongitude(freeMarker.getPosition().longitude);
                        try {
                            getWeatherByLocation(loc);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }else {
                        Toast.makeText(getBaseContext(),getResources().getString(R.string.message_not_select_location),Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        myLocationMarker = mMap.addMarker(myLocationMarkerOptions);
        freeMarker = mMap.addMarker(freeMarkerInitOptions);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(isWaitClickToMap) {
                    freeMarker.setPosition(latLng);
                    freeMarker.setVisible(true);
                    isWaitClickToMap = false;
                    insertRecordIntoHistory();
                }
            }
        });
    }


    public void showWeatherAlert(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(false)
                .setTitle(getResources().getString(R.string.weatherDialogCaption))
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }



    private void showMapTypeSelectorDialog() {
        // Prepare the dialog by setting up a Builder.
        final String fDialogTitle = "Выберите тип карты";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(fDialogTitle);

        // Find the current map type to pre-check the item representing the current state.
        int checkItem = mMap.getMapType() - 1;

        // Add an OnClickListener to the dialog, so that the selection will be handled.
        builder.setSingleChoiceItems(
                MAP_TYPE_ITEMS,
                checkItem,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {
                        // Perform an action depending on which item was selected.
                        switch (item) {
                            case 1:
                                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                break;
                            case 2:
                                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                break;
                            case 3:
                                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                break;
                            default:
                                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        }
                        dialog.dismiss();
                    }
                }
        );
        // Build the dialog and show it.
        AlertDialog fMapTypeDialog = builder.create();
        fMapTypeDialog.setCanceledOnTouchOutside(true);
        fMapTypeDialog.show();
    }


    public void getWeatherByLocation(Location location) throws Exception{
        if(location == null){
            throw new Exception("location is null");
        }
        final Weather result;
        Double lat = location.getLatitude();
        Double lon = location.getLongitude();
        String units = "metric";
        String KEY = getResources().getString(R.string.openWeatherApiKey);
        String language = "ru";

        Call<Weather> currentWeather = weatherApi.getWeather(lat,lon,units,KEY,language);
        currentWeather.enqueue(new Callback<Weather>() {
            @Override
            public void onResponse(Call<Weather> call, Response<Weather> response) {
                Weather weather = response.body();
                StringBuilder textWeather = new StringBuilder();
                textWeather.append(weather.getCity());
                textWeather.append("\n");
                textWeather.append("Температура:");
                textWeather.append(weather.getTemp());
                textWeather.append("\u00B0");
                textWeather.append("С");
                textWeather.append("\n");
                textWeather.append("Максимальная:");
                textWeather.append(weather.getTempMax());
                textWeather.append("\u00B0");
                textWeather.append("С");
                textWeather.append("\n");
                textWeather.append(weather.getWeatherDescription());
                showWeatherAlert(textWeather.toString());
            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {
                Log.d("GPS","Problem with get weather");
                t.printStackTrace();
            }
        });
    }

    public void insertRecordIntoHistory(){

        if (freeMarker != null){
            final HistoryRecord historyRecord = new HistoryRecord();
            historyRecord.lat = freeMarker.getPosition().latitude;
            historyRecord.lon = freeMarker.getPosition().longitude;
            historyRecord.locationName = "";

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    //Find geolocation name
                    String cityName = null;
                    Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
                    List<Address> addresses;
                    try {
                        addresses = gcd.getFromLocation(historyRecord.lat,
                                historyRecord.lon, 1);
                        if (addresses.size() > 0) {
                            cityName = addresses.get(0).getLocality();
                        }
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }

                    if(cityName != null){
                        historyRecord.locationName = cityName;
                    }


                    historyRecordDao.insert(historyRecord);
                    Log.d("GPS","Record added");
                }
            });
            thread.start();
        }
    }



}
