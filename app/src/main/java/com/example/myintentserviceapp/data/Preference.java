package com.example.myintentserviceapp.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity
public class Preference {

    public static final String TAG_SMB_IP = "smbip";
    public static final String TAG_SMB_DIR = "smbdir";
    public static final String TAG_SMB_USER = "smbuser";
    public static final String TAG_SMB_PASS = "smbpass";

    @PrimaryKey()
    @ColumnInfo(name = "param")
    @NotNull
    public String param;

    @ColumnInfo(name = "value")
    public String value;
}
