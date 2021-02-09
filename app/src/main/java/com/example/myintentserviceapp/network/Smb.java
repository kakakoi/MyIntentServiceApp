package com.example.myintentserviceapp.network;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.text.TextUtils;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.example.myintentserviceapp.GridFragment;
import com.example.myintentserviceapp.MyIntentService;
import com.example.myintentserviceapp.R;
import com.example.myintentserviceapp.SettingsActivity;
import com.example.myintentserviceapp.data.Photo;
import com.example.myintentserviceapp.data.PhotoRepository;
import com.example.myintentserviceapp.data.SmbDirectory;
import com.example.myintentserviceapp.data.SmbDirectoryRepository;
import com.example.myintentserviceapp.util.Bitmaps;
import com.example.myintentserviceapp.util.Date;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.util.Properties;

import jcifs.CIFSContext;
import jcifs.CIFSException;
import jcifs.CloseableIterator;
import jcifs.SmbResource;
import jcifs.config.PropertyConfiguration;
import jcifs.context.BaseContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbFile;

public class Smb {
    private static final String TAG = MethodHandles.lookup().lookupClass().getName();
    private static final String SMB_SCHEME = "smb:\\\\";

    private static final String REGEX_IMAGE_FILE = "(?i).*\\.(jpg)";
    //private static final String REGEX_VIDEO_ANDROID = "(?i).*\\.(mp4)";
    //private static final String REGEX_VIDEO_IOS = "(?i).*\\.(mov|mp4)";
    private static final String REGEX_MEDIA_ANDROID = "(?i).*\\.(jpg|mp4)";
    //private static final String REGEX_MEDIA_IOS = "(?i).*\\.(jpg|mov|mp4)";

    public static final String BROADCAST_TAG_STATUS = "smb_broad_cast_tag";

    private String remoteFile;

    private String broadcastTagOutput;
    private String broadcastTagIndex;
    private String broadcastTagAccessDenied;

    private MyIntentService mService = null;
    private Application mApplication = null;

    private PhotoRepository mPhotoRepository;
    private SmbDirectoryRepository mSmbDirectoryRepository;
    //private PreferenceRepository mPreferenceRepository;


    /**
     * 接続用プロパティをセットする
     */
    private void setProperties(Properties p) {
        p.setProperty("jcifs.smb.client.minVersion", "SMB1");
        p.setProperty("jcifs.smb.client.maxVersion", "SMB311");
    }

    /**
     * @return 共有ファイル情報
     */
    private SmbFile connect(CIFSContext auth, String uncPath) {
        try {
            String url = uncPath.replace("\\", "/");
            return new SmbFile(url, auth);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean setup(Application application, MyIntentService service) throws IOException {
        boolean result = false;
        mApplication = application;
        mService = service;
        mPhotoRepository = new PhotoRepository(mApplication);
        mSmbDirectoryRepository = new SmbDirectoryRepository(mApplication);

        broadcastTagOutput = mApplication.getString(R.string.output);
        broadcastTagIndex = mApplication.getString(R.string.index);
        broadcastTagAccessDenied = mApplication.getString(R.string.access_denied);

        //認証情報
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(mApplication /* Activity context */);
        String remoteIp = sharedPreferences.getString("smb_ip", "");
        if (TextUtils.isEmpty(remoteIp)) {
            return result;
        }
        String userName = sharedPreferences.getString("smb_user", "");
        String passWord = sharedPreferences.getString("smb_pass", "");

        String remoteStartDir = sharedPreferences.getString("smb_dir", "");
        remoteFile = SMB_SCHEME + remoteIp + remoteStartDir;

        //基点ディレクトリ登録
        //TODO 再登録防止
        SmbDirectory startDirectory = new SmbDirectory();
        startDirectory.path = remoteFile;
        startDirectory.createdAt = Date.getTime();
        startDirectory.status = SmbDirectory.STATUS_WAITING;
        mSmbDirectoryRepository.insert(startDirectory);

        //接続用プロパティを作成する
        Properties prop = new Properties();
        setProperties(prop);
        SmbFile smbFile = null;

        try {
            //接続情報を作成する
            BaseContext baseContext
                    = new BaseContext(new PropertyConfiguration(prop));
            NtlmPasswordAuthenticator authenticator
                    = new NtlmPasswordAuthenticator(userName, passWord);
            CIFSContext cifsContext = baseContext.withCredentials(authenticator);

            //create index
            SmbDirectory directory;
            while ((directory = mSmbDirectoryRepository.getWaitingTopOne()) != null) {
                //接続する
                smbFile = connect(cifsContext, directory.path);
                try {
                    indexing(cifsContext, smbFile, directory);
                } catch (CIFSException e) {
                    //接続に問題があるのでDBから削除
                    mSmbDirectoryRepository.delete(directory);
                    Log.e(TAG, "Access denied " + directory.path);
                    mService.sendMsgBroadcast(MyIntentService.BROADCAST_ACTION_ERROR, GridFragment.MSG, broadcastTagAccessDenied + directory.path);
                } catch (IOException e) {
                    Log.e(TAG, "IOException" + directory.path + ":" + e.getLocalizedMessage());
                }
            }
            result = true;
        } catch (
                CIFSException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (smbFile != null) {
                smbFile.close();
            }
        }
        return result;
    }

    private void indexing(CIFSContext cifsContext, SmbFile smbFile, SmbDirectory directory) throws IOException {
        if (smbFile == null) {
            //PATHに問題があるのでDBから削除
            //TODO DBが空になった時の処理
            mSmbDirectoryRepository.delete(directory);
        } else {
            directory.indexMediaCount = indexingPhoto(smbFile);
            directory.status = SmbDirectory.STATUS_INDEX;
            mSmbDirectoryRepository.update(directory);

            //load smb file
            int loadCount = 0;
            Photo photo;
            while ((photo = mPhotoRepository.getNoLocalTopOne()) != null) {
                copyLocal(cifsContext, photo);
                loadCount++;
            }
            directory.loadMediaCount = loadCount;
            directory.status = SmbDirectory.STATUS_COMPLETED;
            mSmbDirectoryRepository.update(directory);
        }
    }

    /**
     * Create SMB File Index in DB
     *
     * @param smbFile
     * @return
     * @throws IOException
     */
    private int indexingPhoto(SmbFile smbFile) throws IOException {
        int countFile = 0;
        CloseableIterator<SmbResource> iterator;
        iterator = smbFile.children();

        while (iterator.hasNext()) {
            SmbResource resource = iterator.next();
            SmbFile file = (SmbFile) resource;
            if (resource.isDirectory()) {
                //registration directory
                SmbDirectory directory = new SmbDirectory();
                directory.status = SmbDirectory.STATUS_WAITING;
                directory.path = file.getPath();
                directory.createdAt = Date.getTime();
                mSmbDirectoryRepository.insert(directory);
                Log.d(TAG + ":INSERT UNFINISHED DIRECTORY", directory.path);
            } else if (resource.getName().matches(REGEX_MEDIA_ANDROID)) {
                //create file index
                //作成日取得
                InputStream in = file.getInputStream();
                ExifInterface exifInterface = new ExifInterface(in);
                String dateStr = exifInterface.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);
                if (TextUtils.isEmpty(dateStr)) {
                    java.util.Date createdDate = new java.util.Date(file.createTime());
                    dateStr = Date.format(createdDate);
                }
                //INSERT PHOTO DB
                long startTime = System.currentTimeMillis();
                Photo photo = new Photo();
                photo.dateTimeOriginal = dateStr;
                photo.fileName = resource.getName();
                photo.sourcePath = ((SmbFile) resource).getPath();
                photo.createdAt = Date.getTime();
                photo.sourceType = Photo.SOURCE_TYPE_SMB;
                mPhotoRepository.insert(photo);
                mService.sendMsgBroadcast(MyIntentService.BROADCAST_ACTION_MSG, BROADCAST_TAG_STATUS, broadcastTagIndex + photo.sourcePath);
                countFile++;
                Log.d(TAG + ":INSERT DB", photo.sourcePath);
                Log.d(TAG, "INSERT PHOTO DATA (" + photo.sourcePath + ") time is:" + (System.currentTimeMillis() - startTime));
            }
        }
        return countFile;
    }

    /**
     * copy. SMB to Local
     *
     * @param cifsContext
     * @param photo
     * @throws IOException
     */
    private void copyLocal(CIFSContext cifsContext, Photo photo) throws IOException {
        long startTime = System.currentTimeMillis();
        SmbFile smbFile = connect(cifsContext, photo.sourcePath);
        if (smbFile == null) {
            //PATHに問題があるのでDBから削除
            //TODO DBが空になった時の処理
            mPhotoRepository.delete(photo);
        } else if (smbFile.getName().matches(REGEX_MEDIA_ANDROID)) {
            if (outputFile(smbFile)) {
                photo.localPath = mApplication.getFilesDir() + "/" + photo.fileName;
                mPhotoRepository.update(photo);
                Log.d(TAG, "UPDATE PHOTO DATA (" + photo.sourcePath + ") time is:" + (System.currentTimeMillis() - startTime));
            } else {
                mPhotoRepository.delete(photo);
                Log.d(TAG, "Can Not Output Photo:" + photo.sourcePath);
            }
        } else {
            Log.d(TAG, "Not Photo:" + photo.sourcePath);
        }
    }

    //TODO copy video method
    //private void outputVideo(SmbFile file)

    private boolean outputFile(SmbFile file) throws IOException {
        long startTime = System.currentTimeMillis();

        String fileLength = Long.toString(file.length() / 1024);
        mService.sendMsgBroadcast(MyIntentService.BROADCAST_ACTION_MSG, BROADCAST_TAG_STATUS, broadcastTagOutput + file.getPath() + "[" + fileLength + "KB]");

        boolean result = false;
        //ファイルならローカルにCOPYする
        FileOutputStream fileOut;
        InputStream is;
        try {
            //共有エリアのファイルを読み取りOPENする
            is = file.getInputStream();
            String fileName = file.getName();

            if (fileName.matches(REGEX_IMAGE_FILE)) {
                int bitmapCompressInt = 100;

                fileOut = mApplication.openFileOutput(fileName, Context.MODE_PRIVATE);
                Bitmaps bitmaps = new Bitmaps();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = bitmaps.calcScaleInSampleSize(is);
                is.close();//一度使ったストリームを閉じないと機能しないためクローズしてから再取得
                Bitmap bitmap = BitmapFactory.decodeStream(file.getInputStream(), null, options);
                result = bitmap.compress(Bitmap.CompressFormat.JPEG, bitmapCompressInt, fileOut);
                Log.d(TAG, "outputFile(" + file.getPath() + ") time is:" + (System.currentTimeMillis() - startTime));
                return result;
                //} else if (fileName.matches(REGEX_VIDEO_ANDROID)) {
                //TODO video loader
                /*
                 StorageManager storageManager = mApplication.getSystemService(StorageManager.class);
                 UUID uuid = storageManager.getUuidForPath(mApplication.getCacheDir());
                 long cacheQuota = storageManager.getCacheQuotaBytes(uuid);
                 long targetFileSize = file.length();
                 if (cacheQuota < targetFileSize) {
                 Log.i(TAG, "can not compress video file. disk full... CacheQuotaBytes-" + cacheQuota + " < " + "smb file Bytes-" + file.length());
                 } else {
                 String outputFileStr = mApplication.getCacheDir() + "/" + fileName;
                 OutputStream op = new FileOutputStream(outputFileStr);
                 File.copy(is, op, new Callback() {
                @Override public void call(Object object) {
                long loadLength = (long) object;
                int load = Math.toIntExact(loadLength / 1024 * 1024);
                int target = Math.toIntExact(targetFileSize / 1024 * 1024);

                Log.d(TAG, "Copying Original Video File Size:" + target + "/" + load);
                }
                });
                 String filePath = SiliCompressor.with(mApplication).compressVideo(outputFileStr, String.valueOf(mApplication.getFilesDir()));
                 Path outPutPath = Paths.get(outputFileStr);
                 Files.delete(outPutPath);
                 Log.i(TAG, "success compress video file:" + filePath);
                 result = true;
                 }
                 */
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        Log.d(TAG, "outputFile(" + file.getPath() + ") time is:" + (System.currentTimeMillis() - startTime));
        mService.sendMsgBroadcast(MyIntentService.BROADCAST_ACTION_MSG, BROADCAST_TAG_STATUS, "");

        return result;
    }

    //書込み権限有無
    public boolean checkPermissionWrite(Application application) {
        //認証情報
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(application);
        String remoteIp = sharedPreferences.getString(SettingsActivity.KEY_SMB_IP, "");
        if (TextUtils.isEmpty(remoteIp)) {
            return false;
        }
        String remoteStartDir = sharedPreferences.getString(SettingsActivity.KEY_SMB_DIR, "");
        remoteFile = SMB_SCHEME + remoteIp + remoteStartDir;

        String userName = sharedPreferences.getString(SettingsActivity.KEY_SMB_USER, "");
        String passWord = sharedPreferences.getString(SettingsActivity.KEY_SMB_PASS, "");

        //接続用プロパティを作成する
        Properties prop = new Properties();
        setProperties(prop);


        try {
            //接続情報を作成する
            BaseContext baseContext
                    = new BaseContext(new PropertyConfiguration(prop));
            NtlmPasswordAuthenticator authenticator
                    = new NtlmPasswordAuthenticator(userName, passWord);
            CIFSContext cifsContext = baseContext.withCredentials(authenticator);

            SmbFile smbFile = connect(cifsContext, remoteFile);
            return smbFile.canWrite();
        } catch (CIFSException e) {
            Log.e(TAG, "checkPermissionWrite: ");
        }
        return false;
    }
}
