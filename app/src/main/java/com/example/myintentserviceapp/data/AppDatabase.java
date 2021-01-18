package com.example.myintentserviceapp.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.myintentserviceapp.R;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {User.class, Photo.class, SmbDirectory.class, Preference.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static final String TAG = MethodHandles.lookup().lookupClass().getName();
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    private static volatile Context mContext;
    private static volatile AppDatabase INSTANCE;
    /**
     * Override the onCreate method to populate the database.
     * For this sample, we clear the database every time it is created.
     */
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            databaseWriteExecutor.execute(() -> {
                // Populate the database in the background.
                // If you want to start with more words, just add them.
                //TODO テストデータ削除
/**
                PreferenceDao dao = INSTANCE.preferenceDao();
                Preference preferenceIP = new Preference();
                preferenceIP.param = Preference.TAG_SMB_IP;
                preferenceIP.value = mContext.getString(R.string.preference_smb_ip);
                dao.insert(preferenceIP);

                Preference preferenceDir = new Preference();
                preferenceDir.param = Preference.TAG_SMB_DIR;
                preferenceDir.value = mContext.getString(R.string.preference_smb_dir);
                dao.insert(preferenceDir);

                Preference preferenceUser = new Preference();
                preferenceUser.param = Preference.TAG_SMB_USER;
                preferenceUser.value = mContext.getString(R.string.preference_smb_user);
                dao.insert(preferenceUser);

                Preference preferencePass = new Preference();
                preferencePass.param = Preference.TAG_SMB_PASS;
                preferencePass.value = mContext.getString(R.string.preference_smb_pass);
                dao.insert(preferencePass);
 */
            });
        }
    };

    public static AppDatabase getDatabase(final Context context) {
        mContext = context;
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "app_database")
                            .addCallback(sRoomDatabaseCallback).build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract UserDao userDao();

    public abstract PhotoDao photoDao();

    public abstract SmbDirectoryDao smbDirectoryDao();

    public abstract PreferenceDao preferenceDao();
}
