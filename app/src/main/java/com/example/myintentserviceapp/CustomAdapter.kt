package com.example.myintentserviceapp

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.myintentserviceapp.data.Photo
import com.example.myintentserviceapp.util.GlideApp
import com.example.myintentserviceapp.util.RecyclerFastScroller.FastScrollable
import java.io.File

class CustomAdapter(private var mList: MutableList<Photo>) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>(), FastScrollable {
    private var mParent: ViewGroup? = null
    private var mImageWidth = 0
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        mParent = parent
        mImageWidth = mParent!!.width / 4
        val inflate =
            LayoutInflater.from(mParent!!.context).inflate(R.layout.grid_view, parent, false)
        return ViewHolder(inflate)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pathName = mList[position].localPath
        GlideApp.with(mParent!!.context)
            .load(File(pathName))
            .placeholder(R.drawable.ic_baseline_photo_24)
            .error(R.drawable.ic_baseline_error_outline_24)
            .override(mImageWidth, mImageWidth)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(holder.myImageView)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun add(data: Photo) {
        mList.add(data)
    }

    // Method that executes your code for the action received
    fun onItemClick(view: View, position: Int) {
        val photo = mList[position]
        val filepath = photo.localPath
        val context = mParent!!.context
        val intent = Intent(context, PhotoDetailActivity::class.java)
        intent.putExtra(EXTRA_IMAGE_PATH, filepath)
        intent.putExtra(EXTRA_IMAGE_TITLE, photo.dateTimeOriginal)
        val imageView: View = view.findViewById<View>(R.id.card_image_view) as ImageView
        val sharedElementName = imageView.transitionName
        val activity = context as Activity
        val optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
            activity,
            imageView,
            sharedElementName
        )
        context.startActivity(intent, optionsCompat.toBundle())
        Log.i(
            "TAG",
            ", which is at cell position " + position + ",You clicked number " + getItem(position).toString()
        )
    }

    private fun getItem(position: Int): List<Photo> {
        return mList
    }

    fun setData(newPhotos: MutableList<Photo>) {
        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return mList.size
            }

            override fun getNewListSize(): Int {
                return newPhotos.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return mList[oldItemPosition].id == newPhotos[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return mList[oldItemPosition].createdAt == newPhotos[oldItemPosition].createdAt
            }
        })
        mList = newPhotos
        result.dispatchUpdatesTo(this)
    }

    override fun setBubbleText(position: Int): String {
        return mList[position].dateTimeOriginal
    }

    // Stores and recycles views as they are scrolled off screen
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var myImageView: ImageView
        override fun onClick(view: View) {
            onItemClick(view, adapterPosition)
        }

        init {
            myImageView = itemView.findViewById<View>(R.id.card_image_view) as ImageView
            itemView.setOnClickListener(this)
        }
    }

    companion object {
        const val EXTRA_IMAGE_PATH = "CUSTOM_ADAPTER_IMAGE_PATH"
        const val EXTRA_IMAGE_TITLE = "CUSTOM_ADAPTER_IMAGE_TITLE"
    }
}