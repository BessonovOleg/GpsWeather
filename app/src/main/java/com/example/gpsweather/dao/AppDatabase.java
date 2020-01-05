package com.example.gpsweather.dao;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {HistoryRecord.class}, version = 1,exportSchema = false)
@TypeConverters({DateTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract HistoryRecordDao historyRecordDao();
}
