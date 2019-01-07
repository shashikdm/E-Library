package com.android.project.elibrary;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.Date;

@Dao
public interface DownloadDao {
    @Insert
    void insert(Download download);
    @Query("SELECT bookid FROM Download")
    Integer[] fetch_bookids();
    @Query("SELECT * FROM Download ORDER BY lastread DESC")
    Download[] fetch_recents();
    @Query("DELETE FROM Download WHERE bookid = :bookid")
    void delete_download(int bookid);
    @Query("UPDATE Download SET lastread = :lastread WHERE bookid = :bookid")
    void update_download(int bookid, Date lastread);
}
