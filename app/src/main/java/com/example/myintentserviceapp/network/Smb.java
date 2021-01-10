package com.example.myintentserviceapp.network;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.util.Log;

import com.example.myintentserviceapp.MyIntentService;
import com.example.myintentserviceapp.data.Photo;
import com.example.myintentserviceapp.data.PhotoRepository;
import com.example.myintentserviceapp.data.Preference;
import com.example.myintentserviceapp.data.PreferenceRepository;
import com.example.myintentserviceapp.data.SmbDirectory;
import com.example.myintentserviceapp.data.SmbDirectoryRepository;
import com.example.myintentserviceapp.util.Date;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.util.ArrayList;
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

import static java.sql.DriverManager.println;

public class Smb {
    private static final String TAG = MethodHandles.lookup().lookupClass().getName();
    private String userName;
    private String passWord;
    private String remoteFile;
    private static final String SMB_SCHEME = "smb:\\\\";
    private String remoteIp;
    private String remoteStartDir;

    private SmbFile smbFile = null;


    private MyIntentService mService = null;
    private Application mApplication = null;

    private PhotoRepository mPhotoRepository;
    private SmbDirectoryRepository mSmbDirectoryRepository;
    private PreferenceRepository mPreferenceRepository;

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

    public String getStartRemoteFileName() {
        return remoteFile;
    }

    public int setup(Application application, MyIntentService service) {
        int countImageFile = 0;
        mApplication = application;
        mService = service;
        mPhotoRepository = new PhotoRepository(mApplication);
        mSmbDirectoryRepository = new SmbDirectoryRepository(mApplication);

        //認証情報
        mPreferenceRepository = new PreferenceRepository(mApplication);
        userName = mPreferenceRepository.get(Preference.TAG_SMB_USER).value;
        passWord = mPreferenceRepository.get(Preference.TAG_SMB_PASS).value;
        remoteIp = mPreferenceRepository.get(Preference.TAG_SMB_IP).value;
        remoteStartDir = mPreferenceRepository.get(Preference.TAG_SMB_DIR).value;

        remoteFile = SMB_SCHEME+remoteIp+remoteStartDir;

        //基点ディレクトリ登録
        SmbDirectory startDirectory = new SmbDirectory();
        startDirectory.path = getStartRemoteFileName();
        startDirectory.createdAt = Date.getTime();
        startDirectory.finished = SmbDirectory.UNFINISHED;
        mSmbDirectoryRepository.insert(startDirectory);

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

            SmbDirectory directory = null;
            while ((directory = mSmbDirectoryRepository.getUnFinishedTopOne()) != null) {
                //接続する
                SmbFile smbFile = connect(cifsContext, directory.path);
                if (smbFile == null) {
                    return countImageFile;
                } else {
                    load(smbFile);
                    directory.finished = SmbDirectory.FINISHED;
                    mSmbDirectoryRepository.update(directory);
                }
            }

        } catch (CIFSException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (smbFile != null) {
                smbFile.close();
            }
        }
        return countImageFile;
    }

    private void load(SmbFile smbFile) throws IOException {
        CloseableIterator<SmbResource> iterator = smbFile.children();
        while (iterator.hasNext()) {
            SmbResource resource = iterator.next();
            if (resource.isDirectory()) {
                SmbDirectory directory = new SmbDirectory();
                directory.finished = 0;
                directory.path = ((SmbFile) resource).getPath();
                directory.createdAt = Date.getTime();
                mSmbDirectoryRepository.insert(directory);
                Log.d(TAG + ":INSERT UNFINISHED DIRECTORY", directory.path);
            } else {
                if (resource.getName().matches("(?i).*\\.jpg")) {

                    SmbFile file = (SmbFile) resource;
                    InputStream in = file.getInputStream();
                    ExifInterface exifInterface = new ExifInterface(in);
                    String dateStr = exifInterface.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);
                    outputFile((SmbFile) resource);
                    Photo photo = new Photo();
                    photo.dateTimeOriginal = dateStr;
                    photo.fileName = resource.getName();
                    photo.sourcePath = ((SmbFile) resource).getPath();
                    photo.createdAt = Date.getTime();
                    mPhotoRepository.insert(photo);
                    Log.d(TAG + ":INSERT DB", photo.sourcePath);
                    //mService.sendProgressBroadcast(photo.fileName);
                }
            }
        }
    }

    private boolean outputFile(SmbFile file) throws IOException {
        String outFile = "d:\\temp\\"; //ローカルの複写先フォルダ
        //フォルダは無視
        if (file.isDirectory()) return true;

        //ファイルならローカルにCOPYする
        FileOutputStream fileOut = null;
        InputStream is = null;
        try {
            println(file.getCanonicalUncPath()
                    + " -> " + outFile + file.getName());
            //共有エリアのファイルを読み取りOPENする
            is = file.getInputStream();

            String fileName = file.getName();
            int bitmapCompressInt = 64;

            Bitmap bitmap = BitmapFactory.decodeStream(is);
            fileOut = mApplication.openFileOutput(fileName, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, bitmapCompressInt, fileOut);


        } catch (SmbException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            is.close();
            fileOut.close();
        }
        return false;
    }
}
