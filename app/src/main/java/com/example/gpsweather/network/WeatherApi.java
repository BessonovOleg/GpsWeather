package com.example.gpsweather.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {
    @GET("weather")
    Call<Weather> getWeather(
            @Query("lat") Double lat,
            @Query("lon") Double lon,
            @Query("units") String units,
            @Query("appid") String appid,
            @Query("lang") String language
    );
}
