package com.example.myintentserviceapp.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class PhotoRepository {

    private PhotoDao mPhotoDao;
    private LiveData<List<Photo>> mAllPhotos;
    private LiveData<Integer> mCount;

    public PhotoRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mPhotoDao = db.photoDao();
        mAllPhotos = mPhotoDao.getAllLocal();
        mCount = mPhotoDao.count();
    }


    LiveData<List<Photo>> getAllPhotos() {
        return mAllPhotos;
    }

    LiveData<Integer> count() {
        return mCount;
    }

    public Photo get(Photo photo) {
        return mPhotoDao.get(photo.sourcePath);
    }

    public Photo getFromId(int id) {
        return mPhotoDao.getFromId(id);
    }

    public Photo get(String sourcePath) {
        return mPhotoDao.get(sourcePath);
    }

    public Photo getNoLocalTopOne() {
        return mPhotoDao.getNoLocalTopOne();
    }

    public Photo getNoBackupTopOne() {
        return mPhotoDao.getNoBackupTopOne();
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    public void insert(Photo photo) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mPhotoDao.insert(photo);
        });
    }

    public void delete(Photo photo) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mPhotoDao.delete(photo);
        });
    }

    public void update(Photo photo) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mPhotoDao.update(photo);
        });
    }
}
