package com.example.myintentserviceapp.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity
public class SmbDirectory {

    public static final int STATUS_COMPLETED = 0;
    public static final int STATUS_INDEX = 1;
    public static final int STATUS_WAITING = 2;

    @PrimaryKey()
    @ColumnInfo(name = "path")
    @NotNull
    public String path;

    @ColumnInfo(name = "created_at")
    public String createdAt;

    @ColumnInfo(name = "num_media")
    public int numMedia;

    @ColumnInfo(name = "status")
    public int status;
}
