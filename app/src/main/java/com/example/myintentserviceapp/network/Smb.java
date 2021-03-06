package com.example.myintentserviceapp.network;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.text.TextUtils;
import android.text.format.Formatter;
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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Properties;

import jcifs.CIFSContext;
import jcifs.CIFSException;
import jcifs.CloseableIterator;
import jcifs.SmbResource;
import jcifs.config.PropertyConfiguration;
import jcifs.context.BaseContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

public class Smb {
    private static final String TAG = MethodHandles.lookup().lookupClass().getName();
    private static final String SMB_SCHEME = "smb:\\\\";

    private static final String REGEX_IMAGE_FILE = "(?i).*\\.(jpg)";
    //private static final String REGEX_VIDEO_ANDROID = "(?i).*\\.(mp4)";
    //private static final String REGEX_VIDEO_IOS = "(?i).*\\.(mov|mp4)";
    private static final String REGEX_MEDIA_ANDROID = "(?i).*\\.(jpg|mp4)";
    //private static final String REGEX_MEDIA_IOS = "(?i).*\\.(jpg|mov|mp4)";

    public static final String BROADCAST_TAG_STATUS = "smb_broad_cast_tag";

    private static final int ERROR_CODE_COPY_LOCAL = 101;

    private String broadcastTagOutput;
    private String broadcastTagIndex;
    private String broadcastTagAccessDenied;

    private MyIntentService mService = null;
    private Application mApplication = null;
    private SharedPreferences mSharedPreferences = null;


    private PhotoRepository mPhotoRepository;
    private SmbDirectoryRepository mSmbDirectoryRepository;

    private String mReadSpeedKB = null;



    //TODO serviceなしコンストラクター対応
    public Smb(MyIntentService service) {
        mService = service;
        mApplication = service.getApplication();
        mSharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(mApplication);
        mPhotoRepository = new PhotoRepository(mApplication);
        mSmbDirectoryRepository = new SmbDirectoryRepository(mApplication);
    }

    public Smb(Application application) {
        mApplication = application;
        mSharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(mApplication);
        mPhotoRepository = new PhotoRepository(mApplication);
        mSmbDirectoryRepository = new SmbDirectoryRepository(mApplication);
    }

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

    public boolean setup(MyIntentService service) throws IOException {
        boolean result = false;

        broadcastTagOutput = mApplication.getString(R.string.output);
        broadcastTagIndex = mApplication.getString(R.string.index);
        broadcastTagAccessDenied = mApplication.getString(R.string.access_denied);

        //認証情報
        String remoteFile = getBasePathFromConfig();

        //基点ディレクトリ登録
        //TODO 再登録防止
        SmbDirectory startDirectory = new SmbDirectory();
        startDirectory.path = remoteFile;
        startDirectory.createdAt = Date.getTime();
        startDirectory.status = SmbDirectory.STATUS_WAITING;
        mSmbDirectoryRepository.insert(startDirectory);

        SmbFile smbFile = null;
        try {
            CIFSContext cifsContext = getCIFSContextFromConfig();

            //create index
            SmbDirectory directory;
            while ((directory = mSmbDirectoryRepository.getWaitingTopOne()) != null) {
                //接続する
                smbFile = connect(cifsContext, directory.path);
                try {
                    indexing(smbFile, directory);
                } catch (CIFSException e) {
                    //接続に問題があるのでDBから削除
                    mSmbDirectoryRepository.delete(directory);
                    Log.e(TAG, "Access denied " + directory.path, e);
                    mService.sendMsgBroadcast(MyIntentService.BROADCAST_ACTION_ERROR, GridFragment.MSG, broadcastTagAccessDenied + directory.path);
                } catch (IOException e) {
                    Log.e(TAG, "IOException" + directory.path + ":" + e.getLocalizedMessage());
                }
            }
            //load file
            while ((directory = mSmbDirectoryRepository.getIndexTopOne()) != null) {
                loadAll(cifsContext, directory);
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

    private void indexing(SmbFile smbFile, SmbDirectory directory) throws IOException {
        if (smbFile == null) {
            //PATHに問題があるのでDBから削除
            //TODO DBが空になった時の処理
            mSmbDirectoryRepository.delete(directory);
        } else {
            directory.indexMediaCount = indexingPhoto(smbFile);
            directory.status = SmbDirectory.STATUS_INDEX;
            mSmbDirectoryRepository.update(directory);
        }
    }

    private void loadAll(CIFSContext cifsContext, SmbDirectory directory) throws IOException {
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
                String dateStr = getStringOriginDateTime(in);
                //EXIFになければファイルから作成日時取得
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
                mService.sendMsgBroadcast(MyIntentService.BROADCAST_ACTION_MSG, BROADCAST_TAG_STATUS, photo.fileName);
                countFile++;
                Log.d(TAG + ":INSERT DB", photo.sourcePath);
                Log.d(TAG, "INSERT PHOTO DATA (" + photo.sourcePath + ") time is:" + (System.currentTimeMillis() - startTime));
            }
        }
        return countFile;
    }

    //EXIF datetime origin 文字列取得
    private String getStringOriginDateTime(InputStream in) throws IOException {
        ExifInterface exifInterface = new ExifInterface(in);
        String dateStr = exifInterface.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);
        return dateStr;
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
            String localFileName;
            if (!TextUtils.isEmpty(localFileName = outputFile(smbFile))) {
                photo.localPath = mApplication.getFilesDir() + "/" + localFileName;
                mPhotoRepository.update(photo);
                Log.d(TAG, "UPDATE PHOTO DATA (" + photo.sourcePath + ") time is:" + (System.currentTimeMillis() - startTime));
            } else {
                photo.errorCode = ERROR_CODE_COPY_LOCAL;
                mPhotoRepository.update(photo);
                Log.d(TAG, "Can Not Output. DELETE Photo:" + photo.sourcePath);
            }
        } else {
            Log.d(TAG, "Not Photo:" + photo.sourcePath);
        }
    }

    //TODO copy video method
    //private void outputVideo(SmbFile file)

    private String outputFile(SmbFile file) throws IOException {
        long startTime = System.currentTimeMillis();
        String localFileName = null;

        long fileByte = file.length();

        StringBuilder sb = new StringBuilder();
        sb.append(file.getName());
        sb.append(" ");
        sb.append(Formatter.formatShortFileSize(mApplication, fileByte));

        String msg = sb.toString();

        mService.sendMsgBroadcast(MyIntentService.BROADCAST_ACTION_MSG, BROADCAST_TAG_STATUS, msg);

        //ファイルならローカルにCOPYする
        FileOutputStream fileOut = null;
        InputStream is = null;
        try {
            //共有エリアのファイルを読み取りOPENする
            is = file.getInputStream();
            String fileName = file.getName();

            if (fileName.matches(REGEX_IMAGE_FILE)) {
                int bitmapCompressInt = 100;

                String path = file.getPath();
                String basePath = getBasePathFromConfig().replace("\\", "/");
                String dir = path.replace(basePath, "");
                localFileName = dir.replaceAll("/", "_");

                fileOut = mApplication.openFileOutput(localFileName, Context.MODE_PRIVATE);
                Bitmaps bitmaps = new Bitmaps();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = bitmaps.calcScaleInSampleSize(is);
                is.close();//一度使ったストリームを閉じないと機能しないためクローズしてから再取得
                Bitmap bitmap = BitmapFactory.decodeStream(file.getInputStream(), null, options);
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, bitmapCompressInt, fileOut)) {
                    localFileName = null;
                }
                Log.d(TAG, "outputFile[" + file.getPath() + "] local file name[" + localFileName + "] time is:" + (System.currentTimeMillis() - startTime));

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
        } finally {
            if (file != null) {
                file.close();
            }
            if (fileOut != null) {
                fileOut.close();
            }
            if (is != null) {
                is.close();
            }
        }
        long time = System.currentTimeMillis() - startTime;
        //mReadSpeedKB = String.format("%,.0f", Math.ceil(fileByte / time / 1000));

        Log.d(TAG, "outputFile(" + file.getPath() + ") time is:" + time);
        //mService.sendMsgBroadcast(MyIntentService.BROADCAST_ACTION_MSG, BROADCAST_TAG_STATUS, "");

        return localFileName;
    }

    //書込み権限有無
    public boolean checkPermissionWrite() throws SmbException, IllegalArgumentException {
        SmbFile smbFile = getSmbFileFromConfig();
        boolean result = smbFile.canWrite();
        Log.d(TAG, "checkPermissionWrite return: " + Boolean.toString(result));
        return result;
    }

    //接続からSmbFile取得まで
    public SmbFile getSmbFileFromConfig() throws IllegalArgumentException {
        String path = getBasePathFromConfig();
        return getSmbFile(path);
    }

    public SmbFile getSmbFile(String path) {
        String userName = mSharedPreferences.getString(SettingsActivity.KEY_SMB_USER, "");
        String passWord = mSharedPreferences.getString(SettingsActivity.KEY_SMB_PASS, "");
        return getSmbFile(userName, passWord, path);
    }

    public SmbFile getSmbFile(String userName, String passWord, String path) {
        SmbFile smbFile = null;
        Log.d(TAG, "getSmbFile: user[" + userName + "],path[" + path + "]");

        try {
            CIFSContext cifsContext = getCIFSContext(userName, passWord);

            smbFile = connect(cifsContext, path);
        } catch (CIFSException e) {
            Log.e(TAG, "getSmbFile: ", e);
        }
        return smbFile;
    }

    public CIFSContext getCIFSContext(String userName, String passWord) throws CIFSException {
        //接続用プロパティを作成する
        Properties prop = new Properties();
        setProperties(prop);

        //接続情報を作成する
        BaseContext baseContext
                = new BaseContext(new PropertyConfiguration(prop));
        NtlmPasswordAuthenticator authenticator
                = new NtlmPasswordAuthenticator(userName, passWord);
        return baseContext.withCredentials(authenticator);
    }

    public CIFSContext getCIFSContextFromConfig() throws CIFSException {
        //認証情報
        String userName = mSharedPreferences.getString(SettingsActivity.KEY_SMB_USER, "");
        String passWord = mSharedPreferences.getString(SettingsActivity.KEY_SMB_PASS, "");

        return getCIFSContext(userName, passWord);
    }

    public Photo write(Photo photo) throws IOException {

        InputStream in = new FileInputStream(photo.localPath);
        String dateStr = getStringOriginDateTime(in);
        //EXIFになければファイルから作成日時取得
        if (TextUtils.isEmpty(dateStr)) {
            java.io.File file = new java.io.File(photo.localPath);
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            FileTime time = attrs.creationTime();
            java.util.Date createdDate = new java.util.Date(time.toMillis());
            dateStr = Date.format(createdDate);
        }
        String year = Date.getYear(dateStr);
        String month = Date.getMonth(dateStr);

        String basePath = getBasePathFromConfig();
        String exp = "(\\w)$";
        String rep = "$0/";
        String targetPath = basePath.replaceFirst(exp, rep);
        String appName = mApplication.getString(R.string.app_name).replaceAll("　", " ").replaceAll(" ", "");

        StringBuilder sb = new StringBuilder();
        sb.append(targetPath);
        sb.append(appName);
        sb.append("/");
        sb.append(year);
        sb.append("/");
        sb.append(month);
        sb.append("/");
        String outDir = sb.toString();
        SmbFile smbFile = getSmbFile(outDir);

        if (smbFile.exists()) {
            Log.d(TAG, "write: exists/" + outDir);
            //TODO 書込み処理
        } else {
            smbFile.mkdirs();
            Log.d(TAG, "write: mkdir/" + outDir);
        }
        smbFile.close();

        String sourcePath = outDir + photo.fileName;

        SmbFile outSmbFile = getSmbFile(sourcePath);
        try (FileInputStream inputStream = new FileInputStream(photo.localPath);
             SmbFileOutputStream os = new SmbFileOutputStream(outSmbFile);) {
            byte[] buffer = new byte[1024];
            long total = 0;
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                os.write(buffer, 0, len);
                if (mService != null) {
                    total += len;
                    mService.sendMsgBroadcast(MyIntentService.BROADCAST_ACTION_MSG, BROADCAST_TAG_STATUS, photo.fileName + ":UPLOAD " + total);
                }
            }
            os.flush();
            Log.d(TAG, "write: UPLOAD " + sourcePath);
        } catch (Exception e) {
            Log.e(TAG, "write: ", e);
        } finally {
            if (outSmbFile != null) {
                outSmbFile.close();
            }
        }
        photo.sourcePath = sourcePath;
        Log.d(TAG, "write: UPDATE " + photo.sourcePath);

        return photo;
    }

    private String basePathFromConfig = null;

    //設定から初期パスを取得
    public String getBasePathFromConfig() throws IllegalArgumentException {
        if (!TextUtils.isEmpty(basePathFromConfig)) {
            return basePathFromConfig;
        }
        String remoteIp = mSharedPreferences.getString(SettingsActivity.KEY_SMB_IP, "");
        if (TextUtils.isEmpty(remoteIp)) {
            throw new IllegalArgumentException(SettingsActivity.KEY_SMB_IP + " is empty");
        }
        return SMB_SCHEME + remoteIp + mSharedPreferences.getString(SettingsActivity.KEY_SMB_DIR, "");
    }
}
