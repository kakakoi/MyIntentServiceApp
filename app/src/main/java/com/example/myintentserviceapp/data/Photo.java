package com.example.myintentserviceapp.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity
public class Photo {

    @PrimaryKey()
    @ColumnInfo(name = "source_path")
    @NotNull
    public String sourcePath;

    @ColumnInfo(name = "file_name")
    public String fileName;

    @ColumnInfo(name = "local_path")
    public String localPath;

    @ColumnInfo(name = "gps")
    public String gps;

    @ColumnInfo(name = "height")
    public int height;

    @ColumnInfo(name = "width")
    public int width;

    @ColumnInfo(name = "date_time_original")
    public String dateTimeOriginal;

    @ColumnInfo(name = "created_at")
    public String createdAt;
}