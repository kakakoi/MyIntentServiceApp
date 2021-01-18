package com.example.myintentserviceapp;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.example.myintentserviceapp.network.Smb;

import java.util.ArrayList;

public class MyIntentService extends IntentService {
    private static final String ACTION_SMB = "com.example.myintentserviceapp.action.SMB";

    public MyIntentService() {
        super("MyIntentService");
    }

    public static void startAction(Context context) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_SMB);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SMB.equals(action)) {
                handleActionFoo();
            }
        }
    }

    private void handleActionFoo() {
        Smb smb = new Smb();
        smb.setup(getApplication(), this);
    }

    public void sendProgressBroadcast(String fileName) {
        Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
        intent.putExtra(GridFragment.PHOTO, fileName);
        intent.setPackage(getApplicationInfo().packageName);
        sendBroadcast(intent);
    }
}