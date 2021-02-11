package com.example.myintentserviceapp.util;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;

import com.example.myintentserviceapp.R;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

public class RuntimePermissionUtils {

    // Permissionの確認
    public static boolean checkPermission(@NotNull Activity activity, @NotNull String permission) {
        // 既に許可している
        if (activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        // 許可していない場合、パーミッションの取得を行う
        // 以前拒否されている場合は、なぜ必要かを通知し、手動で許可してもらう
        if (!activity.shouldShowRequestPermissionRationale(permission)) {
            Snackbar snackbar = Snackbar.make(activity.findViewById(R.id.main_activity)
                    , getRequiredMessageString(activity, permission)
                    , Snackbar.LENGTH_INDEFINITE);

            snackbar.setAction(R.string.config_system, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String uriString = "package:" + activity.getPackageName();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse(uriString));
                    activity.startActivity(intent);
                }
            });
            snackbar.show();
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
    public static String getRequiredMessageString(@NotNull Activity activity, @NotNull String permission) {
        String s;
        switch (permission) {
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                s = activity.getString(R.string.android_permission_READ_EXTERNAL_STORAGE);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + permission);
        }
        return s + activity.getString(R.string.required_message);
    }
}
