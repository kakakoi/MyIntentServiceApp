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
public interface SmbDirectoryDao {
    @Query("SELECT * FROM smbDirectory ORDER BY finished ASC")
    LiveData<List<SmbDirectory>> getAll();

    @Query("SELECT * FROM smbDirectory WHERE finished = 0 ORDER BY created_at ASC")
    List<SmbDirectory> getAllUnFinished();

    @Query("SELECT * FROM smbDirectory WHERE finished = 0 ORDER BY created_at ASC LIMIT 1")
    SmbDirectory getUnFinishedTopOne();

    @Query("SELECT * FROM smbDirectory WHERE path = :path")
    LiveData<List<SmbDirectory>> getDirectory(String path);

    @Update
    void update(SmbDirectory... smbDirectory);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(SmbDirectory smbDirectory);

    @Insert
    void insertAll(SmbDirectory... smbDirectories);

    @Delete
    void delete(SmbDirectory smbDirectory);
}
