package com.example.myintentserviceapp.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class PreferenceRepository {

    private PreferenceDao mPreferenceDao;
    private LiveData<List<Preference>> mAllPreferences;

    public PreferenceRepository(Application application) {
        AppDatabase db =AppDatabase.getDatabase(application);
        mPreferenceDao = db.preferenceDao();
        mAllPreferences = mPreferenceDao.getAll();
    }


    LiveData<List<Preference>> getAllPreferences() {
        return mAllPreferences;
    }

    public Preference get(String parama){
        return mPreferenceDao.get(parama);
    }

    public void insert(Preference preference) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mPreferenceDao.insert(preference);
        });
    }
}
