package com.example.myintentserviceapp.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PhotoDao {
    @Query("SELECT * FROM photo ORDER BY date_time_original DESC")
    LiveData<List<Photo>> getAll();

    @Query("SELECT * FROM photo WHERE local_path is NOT NULL or local_path != '' ORDER BY date_time_original DESC")
    LiveData<List<Photo>> getAllLocal();

    @Query("SELECT * FROM photo WHERE source_path = :sourcePath")
    Photo get(String sourcePath);

    @Query("SELECT * FROM photo WHERE source_type = 'smb' AND local_path is null or local_path = '' ORDER BY date_time_original DESC LIMIT 1")
    Photo getNoLocalTopOne();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Photo photo);

    @Update
    void update(Photo... photo);

    @Insert
    void insertAll(Photo... photos);

    @Delete
    void delete(Photo photo);
}
