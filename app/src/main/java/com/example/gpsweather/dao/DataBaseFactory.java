package com.example.gpsweather.dao;

import android.content.Context;

import androidx.room.Room;

public class DataBaseFactory {

    private Context ctx;
    private static AppDatabase database;

    public DataBaseFactory(Context context){
        this.ctx = context;
    }

    public AppDatabase getDataBase(){
        if(database == null){
            database = Room.databaseBuilder(ctx,AppDatabase.class,"database").build();
        }
        return database;
    }
}
