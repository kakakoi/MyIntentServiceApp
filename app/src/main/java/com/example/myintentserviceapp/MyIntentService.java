package com.example.myintentserviceapp;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.myintentserviceapp.media.LocalMedia;
import com.example.myintentserviceapp.network.Smb;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

public class MyIntentService extends IntentService {
    private static final String ACTION_SMB = "com.example.myintentserviceapp.action.SMB";
    private static final String ACTION_LOCAL = "com.example.myintentserviceapp.action.local";
    public static final String BROADCAST_ACTION_ERROR = "com.example.myintentserviceapp.MyIntentService.Broadcast.error";
    public static final String BROADCAST_ACTION_MSG = "com.example.myintentserviceapp.MyIntentService.Broadcast.msg";

    private static final String TAG = MethodHandles.lookup().lookupClass().getName();

    public MyIntentService() {
        super("MyIntentService");
    }

    public static void startActionLocal(Context context) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_LOCAL);
        context.startService(intent);
    }

    public static void startActionSMB(Context context) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_SMB);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SMB.equals(action)) {
                handleActionSMB();
            } else if (ACTION_LOCAL.equals(action)) {
                handleActionLocal();
            }
        }
    }

    private void handleActionLocal() {
        LocalMedia localMedia = new LocalMedia();
        try {
            localMedia.indexing(getApplication());
        } catch (Exception e) {
            Log.e(TAG, "handleActionLocal: ", e);
            Toast.makeText(this, "例外が発生、Permissionを許可していますか？", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleActionSMB() {
        Smb smb = new Smb();
        try {
            if (!smb.setup(getApplication(), this)) {
                sendMsgBroadcast(BROADCAST_ACTION_ERROR, GridFragment.MSG, getString(R.string.error_cifs));
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getLocalizedMessage());
            sendMsgBroadcast(BROADCAST_ACTION_ERROR, GridFragment.MSG, getString(R.string.error_cifs));
        }
    }

    public void sendMsgBroadcast(String broadcastAction, String extendedName, String msg) {
        Intent intent = new Intent(broadcastAction);
        intent.putExtra(extendedName, msg);
        intent.setPackage(getApplicationInfo().packageName);
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
    }
}