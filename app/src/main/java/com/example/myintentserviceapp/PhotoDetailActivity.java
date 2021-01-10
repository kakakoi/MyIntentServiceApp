package com.example.myintentserviceapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.myintentserviceapp.data.Photo;
import com.example.myintentserviceapp.data.PhotoRepository;

import java.io.File;

public class PhotoDetailActivity extends AppCompatActivity {

    private ImageView photoDetailImageView;
    private TextView photoDetailTitleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);
        Intent intent = getIntent();
        String imageKey = intent.getStringExtra(CustomAdapter.EXTRA_IMAGE_KEY);
        String imageTitle = intent.getStringExtra(CustomAdapter.EXTRA_IMAGE_TITLE);

        photoDetailTitleTextView = (TextView) findViewById(R.id.photo_detail_title);
        photoDetailTitleTextView.setText(imageTitle);

        photoDetailImageView = (ImageView) findViewById(R.id.photo_detail_image_view);

        Glide.with(this)
                .load(new File(imageKey))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(photoDetailImageView);
    }


}