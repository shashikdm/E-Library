package com.android.project.elibrary;

import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.Objects;


public class PdfRead extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener {
    PDFView pdfview;
    private int pageNumber;
    Integer bookid;
    private final static String KEY_CURRENT_PAGE = "current_page";
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_pdf_read);
        new Synchronise().execute();
        if (savedInstanceState != null)
        {
            pageNumber = savedInstanceState.getInt(KEY_CURRENT_PAGE);
        }
        else
        {
            pageNumber = -1;
        }
        pdfview = findViewById(R.id.pdfread);
        bookid = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("bookid")).toString());
        String filename= Objects.requireNonNull(getApplicationContext().getExternalFilesDir("")).getAbsolutePath()+File.separator+Environment.DIRECTORY_DOWNLOADS+File.separator+bookid.toString();
        File file = new File(filename);
        byte[] byteform = new byte[(int) file.length()];
        try {
            FileInputStream fis = new FileInputStream(file);
            fis.read(byteform); //read file into bytes[]
            fis.close();
        } catch(Exception e) {
            Snackbar.make(findViewById(R.id.openbook), e.toString(), Snackbar.LENGTH_LONG).show();
        }
        byteform[0] = (byte)(-byteform[0]);
        pdfview.fromBytes(byteform).defaultPage(pageNumber)
                .onPageChange(this)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(1)
                .load();
    }
    @Override
    public void onPageChanged(int page, int pageCount){
        pageNumber = page;
    }
    @Override
    public void loadComplete(int nbPages) {
        if (pageNumber >= 0)
        {
            pdfview.jumpTo(pageNumber);
        }
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        pageNumber = savedInstanceState.getInt(KEY_CURRENT_PAGE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_PAGE, pageNumber);
    }
    @SuppressLint("StaticFieldLeak")
    private class Synchronise extends AsyncTask<Void ,Void,Integer> {
        @Override
        protected Integer doInBackground(Void... updates) {
            LocalDB localDB = Room.databaseBuilder(getApplicationContext(), LocalDB.class, "localDB").build();
            try {
                localDB.downloadDao().update_download(bookid, Calendar.getInstance().getTime());
            } catch (Exception e) {
            }
            return 1;
        }
        @Override
        protected void onPostExecute(Integer n) {
        }
    }
}