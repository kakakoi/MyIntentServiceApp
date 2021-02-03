package com.example.myintentserviceapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myintentserviceapp.util.RuntimePermissionUtils;

public class MainActivity extends AppCompatActivity {
    public static final String BROADCAST_ACTION = "com.example.myintentserviceapp.MainActivity.broadcast";
    private static final String TAG = "MainActivity";
    private final int EXTERNAL_STORAGE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        if (RuntimePermissionUtils.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            MyIntentService.startActionLocal(this);
        }
        MyIntentService.startActionSMB(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);

                return true;

            case R.id.action_sync:
                MyIntentService.startActionSMB(this);
                if (RuntimePermissionUtils.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    MyIntentService.startActionLocal(this);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // 結果の受け取り
    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // requestPermissionsの引数に指定した値が、requestCodeで返却される
        if (requestCode != EXTERNAL_STORAGE_REQUEST_CODE) {
            return;
        }
        // 自分がリクエストしたコードで戻ってきた場合
        // 使用が許可された
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // ローカルファイルの読み取り処理実行
            MyIntentService.startActionLocal(this);
            return;
        }
        // 拒否されたが永続的ではない場合
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "許可されないとアプリが実行できません", Toast.LENGTH_SHORT).show();
            // パーミッションの取得を依頼
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_REQUEST_CODE);
            return;
        }
        // 永続的に拒否された場合
        Toast.makeText(this, "許可されないとアプリが実行できません\nアプリ設定＞権限をチェックしてください", Toast.LENGTH_SHORT).show();
    }
}