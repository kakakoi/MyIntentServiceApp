package com.example.myintentserviceapp.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PreferenceDao {

    @Query("SELECT * FROM preference")
    LiveData<List<Preference>> getAll();

    @Query("SELECT * FROM preference WHERE param = :param")
    Preference get(String param);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Preference Preference);

    @Insert
    void insertAll(Preference... preferences);

    @Delete
    void delete(Preference preference);
}
