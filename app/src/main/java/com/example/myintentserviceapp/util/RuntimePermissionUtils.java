package com.example.myintentserviceapp.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.example.myintentserviceapp.R;

import org.jetbrains.annotations.NotNull;

public class RuntimePermissionUtils {

    public static final String REQUIRED_MESSAGE_KEY = "required_message";

    // Permissionの確認
    public static boolean checkPermission(@NotNull Activity activity, @NotNull String permission) {
        // 既に許可している
        if (activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        // 許可していない場合、パーミッションの取得を行う
        // 以前拒否されている場合は、なぜ必要かを通知し、手動で許可してもらう
        if (!activity.shouldShowRequestPermissionRationale(permission)) {
            Toast.makeText(activity, getRequiredMessageString(activity, permission), Toast.LENGTH_LONG).show();
        }
        return false;
    }

    /**
     * 必須権限許可催促文言取得
     *
     * @param activity
     * @param permission
     * @return
     */
    public static String getRequiredMessageString(@NotNull Activity activity, @NotNull String permission){
        String s;
        switch (permission){
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                s = activity.getString(R.string.android_permission_READ_EXTERNAL_STORAGE);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + permission);
        }
        return s + activity.getString(R.string.required_message);
    }
}
