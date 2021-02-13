package com.example.myintentserviceapp

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.myintentserviceapp.data.PhotoRepository
import com.example.myintentserviceapp.util.GlideApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PhotoDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_detail)
        val intent = intent
        val id = intent.getIntExtra(CustomAdapter.EXTRA_IMAGE_ID, 0)
        if (id > 0) {
            GlobalScope.launch {
                val photoRepository = PhotoRepository(application)
                val photo = photoRepository.getFromId(id)
                 var titleTextView = findViewById<View>(R.id.photo_detail_title) as TextView
                titleTextView!!.text = photo.fileName
                var datetimeTextView = findViewById<View>(R.id.photo_detail_datetime) as TextView
                datetimeTextView!!.text = photo.dateTimeOriginal
                var typeTextView = findViewById<View>(R.id.photo_detail_type) as TextView
                typeTextView!!.text = photo.sourceType
                var sourcePathTextView = findViewById<View>(R.id.photo_detail_source_path) as TextView
                sourcePathTextView!!.text = photo.sourcePath
                var localPathTextView = findViewById<View>(R.id.photo_detail_local_path) as TextView
                localPathTextView!!.text = photo.localPath

                var photoDetailImageView = findViewById<View>(R.id.photo_detail_image_view) as ImageView
                withContext(Dispatchers.Main) {
                    GlideApp.with(application)
                        .load(File(photo.localPath))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(photoDetailImageView!!)
                }
            }
        }
    }
}