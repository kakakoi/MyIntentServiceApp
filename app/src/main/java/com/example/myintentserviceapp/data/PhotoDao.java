package com.example.myintentserviceapp.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PhotoDao {
    @Query("SELECT * FROM photo ORDER BY date_time_original ASC")
    LiveData<List<Photo>> getAll();

    @Query("SELECT * FROM photo WHERE source_path = :sourcePath")
    Photo get(String sourcePath);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Photo photo);

    @Insert
    void insertAll(Photo... photos);

    @Delete
    void delete(Photo photo);
}
