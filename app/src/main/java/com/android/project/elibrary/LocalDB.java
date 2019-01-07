package com.android.project.elibrary;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

@Database(entities = {Download.class}, version = 1, exportSchema = false)
@TypeConverters({Converter.class})
public abstract class LocalDB extends RoomDatabase {
    public abstract DownloadDao downloadDao();
}
