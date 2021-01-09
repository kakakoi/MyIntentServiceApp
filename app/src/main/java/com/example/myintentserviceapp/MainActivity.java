package com.example.myintentserviceapp;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public static final String BROADCAST_ACTION = " jp.co.casareal.genintentservice.broadcast";
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * クリック・イベント対応コールバック・メソッド
     */
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_intentservice_smb:
                MyIntentService.startActionFoo(this);
                break;
        }
    }
}