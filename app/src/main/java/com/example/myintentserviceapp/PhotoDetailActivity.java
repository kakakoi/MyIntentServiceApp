package com.example.myintentserviceapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;

public class PhotoDetailActivity extends AppCompatActivity {

    private ImageView photoDetailImageView;
    private TextView photoDetailTitleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);
        Intent intent = getIntent();
        String filepath = intent.getStringExtra(CustomAdapter.EXTRA_IMAGE_PATH);
        String imageTitle = intent.getStringExtra(CustomAdapter.EXTRA_IMAGE_TITLE);

        photoDetailTitleTextView = (TextView) findViewById(R.id.photo_detail_title);
        photoDetailTitleTextView.setText(imageTitle);

        photoDetailImageView = (ImageView) findViewById(R.id.photo_detail_image_view);

        Glide.with(this)
                .load(new File(filepath))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(photoDetailImageView);
    }


}