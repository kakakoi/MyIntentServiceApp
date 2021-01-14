package com.example.myintentserviceapp.data;

import android.app.Application;

import java.util.List;

public class SmbDirectoryRepository {
    private SmbDirectoryDao mSmbDirectoryDao;
    private List<SmbDirectory> mAllUnFinishedSmbDirectories;

    public SmbDirectoryRepository(Application application) {
        AppDatabase db =AppDatabase.getDatabase(application);
        mSmbDirectoryDao = db.smbDirectoryDao();
        mAllUnFinishedSmbDirectories = mSmbDirectoryDao.getAllWaiting();
    }


    List<SmbDirectory> getAllUnFinishedSmbDirectories() {
        return mAllUnFinishedSmbDirectories;
    }

    public SmbDirectory getWaitingTopOne(){
        return mSmbDirectoryDao.getWaitingTopOne();
    }

    public SmbDirectory getIndexTopOne(){
        return mSmbDirectoryDao.getIndexTopOne();
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    public void insert(SmbDirectory smbDirectory) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mSmbDirectoryDao.insert(smbDirectory);
        });
    }

    public void update(SmbDirectory smbDirectory) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mSmbDirectoryDao.update(smbDirectory);
        });
    }

    public void delete(SmbDirectory smbDirectory){
        AppDatabase.databaseWriteExecutor.execute(()->{
            mSmbDirectoryDao.delete(smbDirectory);
        });
    }
}
