package com.example.gpsweather.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface HistoryRecordDao {
    @Query("select * from historyrecord")
    List<HistoryRecord> getAll();

    @Query("select * from historyrecord where id = :id")
    HistoryRecord getByID(long id);

    @Insert
    void insert(HistoryRecord historyRecord);

    @Update
    void update(HistoryRecord historyRecord);

    @Delete
    void delete(HistoryRecord historyRecord);
}
