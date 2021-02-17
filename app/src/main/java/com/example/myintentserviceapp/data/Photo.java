package com.example.myintentserviceapp.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index(value = "source_path",unique = true), @Index(value = "local_path",unique = true)})
public class Photo {

    public static final String SOURCE_TYPE_SMB = "smb";
    public static final String SOURCE_TYPE_LOCAL = "local";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "source_path")
    public String sourcePath;

    @ColumnInfo(name = "source_type")
    public String sourceType;

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

    @ColumnInfo(name = "error_code")
    public int errorCode;
}
