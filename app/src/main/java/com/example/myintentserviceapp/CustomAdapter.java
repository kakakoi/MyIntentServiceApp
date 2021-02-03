package com.example.myintentserviceapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.myintentserviceapp.data.Photo;
import com.example.myintentserviceapp.util.RecyclerFastScroller;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> implements RecyclerFastScroller.FastScrollable {

    public static final String EXTRA_IMAGE_PATH = "CUSTOM_ADAPTER_IMAGE_PATH";
    public static final String EXTRA_IMAGE_TITLE = "CUSTOM_ADAPTER_IMAGE_TITLE";

    private List<Photo> mList;
    private ViewGroup mParent;
    private int mImageWidth;

    public CustomAdapter(List<Photo> list) {
        this.mList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mParent = parent;
        mImageWidth = mParent.getWidth() / 4;
        View inflate = LayoutInflater.from(mParent.getContext()).inflate(R.layout.grid_view, parent, false);
        ViewHolder vh = new ViewHolder(inflate);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String pathName = mList.get(position).localPath;

        Glide.with(mParent.getContext())
                .load(new File(pathName))
                .override(mImageWidth, mImageWidth)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(((ViewHolder) holder).myImageView);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void add(Photo data) {
        mList.add(data);
    }

    // Method that executes your code for the action received
    public void onItemClick(View view, int position) {
        Photo photo = mList.get(position);

        String filepath = photo.localPath;
        Context context = mParent.getContext();
        Intent intent = new Intent(context, PhotoDetailActivity.class);
        intent.putExtra(EXTRA_IMAGE_PATH, filepath);
        intent.putExtra(EXTRA_IMAGE_TITLE,photo.dateTimeOriginal);

        View imageView = (ImageView) view.findViewById(R.id.card_image_view);
        String sharedElementName = imageView.getTransitionName();
        Activity activity = (Activity) context;
        ActivityOptionsCompat optionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity, imageView, sharedElementName);

        context.startActivity(intent, optionsCompat.toBundle());
        Log.i("TAG", ", which is at cell position " + position + ",You clicked number " + getItem(position).toString());
    }

    private List<Photo> getItem(int position) {
        return mList;
    }

    public void setData(final List<Photo> newPhotos) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return mList.size();
            }

            @Override
            public int getNewListSize() {
                return newPhotos.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return mList.get(oldItemPosition).sourcePath.equals(newPhotos.get(newItemPosition).sourcePath);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return mList.get(oldItemPosition).createdAt.equals(newPhotos.get(oldItemPosition).createdAt);
            }
        });
        this.mList = newPhotos;
        result.dispatchUpdatesTo(this);
    }

    @NotNull
    @Override
    public String setBubbleText(int position) {
        return mList.get(position).dateTimeOriginal;
    }

    // Stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView myImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            myImageView = (ImageView) itemView.findViewById(R.id.card_image_view);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemClick(view, getAdapterPosition());
        }
    }
}
