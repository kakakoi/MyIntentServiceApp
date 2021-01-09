package com.example.myintentserviceapp.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity
public class SmbDirectory {

    public static final int FINISHED = 1;
    public static final int UNFINISHED = 0;

    @PrimaryKey()
    @ColumnInfo(name = "path")
    @NotNull
    public String path;

    @ColumnInfo(name = "created_at")
    public String createdAt;

    @ColumnInfo(name = "finished")
    public int finished;
}
