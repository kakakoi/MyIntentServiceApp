package com.example.myintentserviceapp.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class PhotoRepository {

    private PhotoDao mPhotoDao;
    private LiveData<List<Photo>> mAllPhotos;

    public PhotoRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mPhotoDao = db.photoDao();
        mAllPhotos = mPhotoDao.getAllLocal();
    }


    LiveData<List<Photo>> getAllPhotos() {
        return mAllPhotos;
    }

    public Photo get(Photo photo) {
        return mPhotoDao.get(photo.sourcePath);
    }

    public Photo get(String sourcePath) {
        return mPhotoDao.get(sourcePath);
    }

    public Photo getNoLocalTopOne() {
        return mPhotoDao.getNoLocalTopOne();
    }

    LiveData<List<Photo>> getReAllPhotos() {
        mAllPhotos = mPhotoDao.getAll();
        return mAllPhotos;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    public void insert(Photo photo) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mPhotoDao.insert(photo);
        });
    }

    public void delete(Photo photo){
        AppDatabase.databaseWriteExecutor.execute(()->{
            mPhotoDao.delete(photo);
        });
    }

    public void update(Photo photo){
        AppDatabase.databaseWriteExecutor.execute(()->{
            mPhotoDao.update(photo);
        });
    }
}
