package com.example.myintentserviceapp.media;

import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.example.myintentserviceapp.data.Photo;
import com.example.myintentserviceapp.data.PhotoRepository;
import com.example.myintentserviceapp.util.Date;

import java.lang.invoke.MethodHandles;

public class LocalMedia {
    private static final String TAG = MethodHandles.lookup().lookupClass().getName();

    public void indexing(Application application) {
        // 取得するフィールド
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.SIZE
        };
        // 検索条件
        // mimetypeで検索 ?はプレースフォルダーで、値はselectionArgsで指定
        String selection = MediaStore.Files.FileColumns.MIME_TYPE + "=?";
        ;
        // 検索条件に指定する値
        // まずjpgのmimetypeを取得
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("jpg");
        // 検索条件の値として設定
        String[] selectionArgs = new String[]{mimeType};
        // _IDの降順でソート
        String sortOrder = MediaStore.Images.Media._ID + " desc";
        ContentResolver contentResolver = application.getContentResolver();
        Cursor cursor = null;
        PhotoRepository photoRepository = new PhotoRepository(application);

        try {
            cursor = contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortOrder);
        } catch (Exception e) {
            Log.e(TAG, "indexing: ", e);
            throw e;
        }

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                int dateAdded = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));

                Photo photo = new Photo();
                photo.sourceType = Photo.SOURCE_TYPE_LOCAL;
                photo.localPath = filePath;
                photo.dateTimeOriginal = Date.formatMediaDateAdded(dateAdded);
                photo.createdAt = Date.getTime();
                photoRepository.insert(photo);

            } while (cursor.moveToNext());

            cursor.close();
        }

    }
}
