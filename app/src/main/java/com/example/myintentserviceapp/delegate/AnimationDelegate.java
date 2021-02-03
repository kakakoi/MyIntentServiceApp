package com.example.myintentserviceapp.delegate;

import android.app.Activity;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.view.menu.ActionMenuItemView;

import com.example.myintentserviceapp.R;

import org.jetbrains.annotations.NotNull;

public class AnimationDelegate {
    public static void startAnimSyncIcon(@NotNull Activity activity, @NotNull MenuItem item) {
        ActionMenuItemView menuItemView = (ActionMenuItemView) activity.findViewById(R.id.action_sync);
        startAnimSyncIcon(activity, item, menuItemView);
    }

    public static void startAnimSyncIcon(@NotNull Activity activity, @NotNull MenuItem item, @NotNull  ActionMenuItemView menuItemView) {
        item.setIcon(R.drawable.ic_baseline_sync_24);
        Animation animation = AnimationUtils.loadAnimation(activity, R.anim.rotaion);

        menuItemView.startAnimation(animation);
    }
}