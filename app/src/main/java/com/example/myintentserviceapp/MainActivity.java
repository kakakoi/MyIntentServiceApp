package com.example.myintentserviceapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {
    public static final String BROADCAST_ACTION = "com.example.myintentserviceapp.MainActivity.broadcast";
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

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
                MyIntentService.startAction(this);
                item.setIcon(R.drawable.ic_baseline_sync_24);
                Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotaion);

                ActionMenuItemView menuItemView = (ActionMenuItemView) findViewById(R.id.action_sync);
                menuItemView.startAnimation(animation);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}