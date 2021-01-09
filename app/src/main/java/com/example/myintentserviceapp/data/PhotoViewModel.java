package com.example.myintentserviceapp.data;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class PhotoViewModel extends AndroidViewModel {

    private LiveData<List<Photo>> mAllPhotos;
    private PhotoRepository mPhotoRepository;

    public PhotoViewModel(@NonNull Application application) {
        super(application);
        mPhotoRepository = new PhotoRepository(application);
        mAllPhotos = mPhotoRepository.getAllPhotos();
    }

    public LiveData<List<Photo>> getAllPhotos() {
        return mAllPhotos;
    }

    public void reloadAllPhotos(){
        mAllPhotos = mPhotoRepository.getReAllPhotos();
    }

    void insert(Photo photo) {
        mPhotoRepository.insert(photo);
    }
}
