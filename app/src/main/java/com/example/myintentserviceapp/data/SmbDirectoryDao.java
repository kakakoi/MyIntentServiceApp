package com.example.myintentserviceapp.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * status completed 0,index 1,waiting 2
 */
@Dao
public interface SmbDirectoryDao {
    @Query("SELECT * FROM smbDirectory ORDER BY status ASC")
    LiveData<List<SmbDirectory>> getAll();

    @Query("SELECT * FROM smbDirectory WHERE status = 2 ORDER BY created_at ASC")
    List<SmbDirectory> getAllWaiting();

    @Query("SELECT * FROM smbDirectory WHERE status = 1 ORDER BY created_at ASC LIMIT 1")
    SmbDirectory getIndexTopOne();

    @Query("SELECT * FROM smbDirectory WHERE status = 2 ORDER BY created_at ASC LIMIT 1")
    SmbDirectory getWaitingTopOne();

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

    @Query("DELETE FROM smbdirectory WHERE path = :path")
    void deleteById(String path);

    @Query("DELETE FROM smbdirectory")
    void deleteAll();
}
