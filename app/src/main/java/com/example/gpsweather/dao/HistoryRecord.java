package com.example.gpsweather.dao;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;


@Entity
public class HistoryRecord implements Serializable{
    @PrimaryKey(autoGenerate = true)
    public long id;
    public Date dateEvent;
    public String locationName;
    public double lat;
    public double lon;

    public HistoryRecord(){
        dateEvent = new Date();
    }

    @Override
    public String toString() {
        return "HistoryRecord{" +
                "id=" + id +
                ", dateEvent=" + dateEvent +
                ", locationName='" + locationName + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                '}';
    }
}