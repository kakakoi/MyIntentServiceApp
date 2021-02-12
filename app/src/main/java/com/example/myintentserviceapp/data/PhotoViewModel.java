package com.example.myintentserviceapp.data;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class PhotoViewModel extends AndroidViewModel {

    private LiveData<List<Photo>> mAllPhotos;
    private LiveData<Integer> mCount;
    private PhotoRepository mPhotoRepository;

    public PhotoViewModel(@NonNull Application application) {
        super(application);
        mPhotoRepository = new PhotoRepository(application);
        mAllPhotos = mPhotoRepository.getAllPhotos();
        mCount = mPhotoRepository.count();

    }

    public LiveData<List<Photo>> getAllPhotos() {
        return mAllPhotos;
    }

    public LiveData<Integer> getCount(){
        return mCount;
    }

    void insert(Photo photo) {
        mPhotoRepository.insert(photo);
    }
}
