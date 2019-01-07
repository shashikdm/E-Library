package com.android.project.elibrary;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;

@Entity
public class Download {
    @PrimaryKey
    @NonNull
    int bookid;
    @ColumnInfo(name = "name")
    String name;
    @ColumnInfo(name = "lastread")
    Date lastread;
}
