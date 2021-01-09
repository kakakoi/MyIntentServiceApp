package com.example.myintentserviceapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.myintentserviceapp.data.Photo;

import java.io.File;
import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

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
        mImageWidth = mParent.getWidth()/4;
        View inflate = LayoutInflater.from(mParent.getContext()).inflate(R.layout.grid_view, parent, false);
        ViewHolder vh = new ViewHolder(inflate);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String pathName = mParent.getContext().getFilesDir() + "/" + mList.get(position).fileName;
        //Bitmap bitmap = BitmapFactory.decodeFile(pathName);
        Glide.with(mParent.getContext())
                .load(new File(pathName))
                .override(mImageWidth, mImageWidth)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(((ViewHolder) holder).myImageView);
        //holder.myImageView.setImageBitmap(bitmap);
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
        //Log.i("TAG", "You clicked number " + getItem(position).toString() + ", which is at cell position " + position);
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

    public void setData(final List<Photo> newPhotos){
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
}