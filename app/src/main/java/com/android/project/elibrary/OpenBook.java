package com.android.project.elibrary;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class OpenBook extends AppCompatActivity {
    String  title, downloadlink;
    Integer bookid;
    FloatingActionButton downloadb, openb , deleteb ;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new Synchronise1().execute();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_book);
        downloadb = findViewById(R.id.downloadb);
        openb = findViewById(R.id.openb);
        deleteb = findViewById(R.id.deleteb);
        String department =  Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("department")).toString();
        String semester =  Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("semester")).toString();
        String courseid =  Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("courseid")).toString();
        downloadlink = Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("link")).toString();
        title = Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("name")).toString();
        bookid = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("bookid")).toString());

        TextView textview = findViewById(R.id.topbar);
        textview.setText(department+" > "+semester+" > "+courseid+" >");
        textview = findViewById(R.id.title);
        textview.setText(title);
        new Synchronise1().execute();
    }
    @SuppressLint("StaticFieldLeak")
    private class Synchronise1 extends AsyncTask<Void,Void,Integer> {
        @Override
        protected Integer doInBackground(Void... data) {
            LocalDB localDB = Room.databaseBuilder(getApplicationContext(), LocalDB.class, "localDB").build();
            Integer[] bookids = localDB.downloadDao().fetch_bookids();
            if(Arrays.asList(bookids).contains(bookid)) {
                return 1;
            }
            return 0;
        }
        @Override
        protected void onPostExecute(Integer exists) {
            try {
                if (exists == 1) {
                    openb.setClickable(true);
                    openb.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.design_default_color_primary)));
                    deleteb.setClickable(true);
                    deleteb.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent)));
                    downloadb.setClickable(false);
                    downloadb.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                } else {
                    downloadb.setClickable(true);
                    downloadb.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));
                    openb.setClickable(false);
                    openb.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                    deleteb.setClickable(false);
                    deleteb.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                }
            } catch(Exception e) {
                //Do nothing
            }
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class Synchronise2 extends AsyncTask<Void,Void,Integer> {
        @Override
        protected Integer doInBackground(Void... data) {
            LocalDB localDB = Room.databaseBuilder(getApplicationContext(), LocalDB.class, "localDB").build();
            Download download = new Download();
            download.bookid = bookid;
            download.name = title;
            download.lastread = Calendar.getInstance().getTime();
            String filename= Objects.requireNonNull(getApplicationContext().getExternalFilesDir("")).getAbsolutePath()+File.separator+Environment.DIRECTORY_DOWNLOADS+File.separator+bookid.toString();
            File file = new File(filename);
            byte[] byteform = new byte[(int) file.length()];
            try {
                FileInputStream fis = new FileInputStream(file);
                //noinspection ResultOfMethodCallIgnored
                fis.read(byteform); //read file into bytes[]
                fis.close();
            } catch(Exception e) {
                return 0;
            }
            byteform[0] = (byte)(-byteform[0]);
            //noinspection ResultOfMethodCallIgnored
            file.delete();
            try {
                FileOutputStream fileOuputStream = new FileOutputStream(filename);
                fileOuputStream.write(byteform);
                fileOuputStream.close();
            } catch (IOException ignored) {
            }
            localDB.downloadDao().insert(download);
            return 1;
        }
        @Override
        protected void onPostExecute(Integer exists) {
            try {
                if (exists == 1) {
                    openb.setClickable(true);
                    openb.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.design_default_color_primary)));
                    deleteb.setClickable(true);
                    deleteb.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent)));
                    downloadb.setClickable(false);
                    downloadb.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));

                } else {
                    downloadb.setClickable(true);
                    downloadb.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));
                    openb.setClickable(false);
                    openb.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                    deleteb.setClickable(false);
                    deleteb.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                }
            } catch(Exception ignored) {
            }
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class Synchronise3 extends AsyncTask<Void,Void,Integer> {
        @Override
        protected Integer doInBackground(Void... data) {
            LocalDB localDB = Room.databaseBuilder(getApplicationContext(), LocalDB.class, "localDB").build();
            localDB.downloadDao().delete_download(bookid);
            return 1;
        }
        @Override
        protected void onPostExecute(Integer exists) {
        }
    }
    public void downloadbook(View view) {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        if(!(activeNetworkInfo!=null && activeNetworkInfo.isConnected())) {
            Snackbar.make(findViewById(R.id.openbook),"No Internet", Snackbar.LENGTH_LONG).show();
            return;
        }
        downloadb.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        downloadb.setClickable(false);
        Snackbar.make(findViewById(R.id.openbook),"Downloading",Snackbar.LENGTH_LONG).show();
        Uri link = Uri.parse(downloadlink);
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        final DownloadManager.Request request = new DownloadManager.Request(link);
        request.setTitle("Downloading");
        request.setDescription(title);
        request.setDestinationInExternalFilesDir(getApplicationContext(), Environment.DIRECTORY_DOWNLOADS, bookid.toString());
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);

        dm.enqueue(request);
        BroadcastReceiver onComplete=new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {
                String action = intent.getAction();
                assert action != null;
                if(action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0));
                    DownloadManager manager = (DownloadManager) ctxt.getSystemService(Context.DOWNLOAD_SERVICE);
                    Cursor cursor = manager.query(query);
                    if (cursor.moveToFirst()) {
                        if (cursor.getCount() > 0) {
                            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                String filename= Objects.requireNonNull(getApplicationContext().getExternalFilesDir("")).getAbsolutePath()+File.separator+Environment.DIRECTORY_DOWNLOADS+File.separator+bookid.toString();
                                File file = new File(filename);
                                byte[] byteform = new byte[(int) file.length()];
                                try {
                                    FileInputStream fis = new FileInputStream(file);
                                    //noinspection ResultOfMethodCallIgnored
                                    fis.read(byteform); //read file into bytes[]
                                    fis.close();
                                } catch(Exception ignored) {
                                }
                                StringBuilder type = new StringBuilder();
                                for(int i = 0; i < 4; i++) {
                                    type.append((char)byteform[i]);
                                }
                                if(type.toString().equalsIgnoreCase("%PDF")) {
                                    try {
                                        Snackbar.make(findViewById(R.id.openbook), "Download complete", Snackbar.LENGTH_LONG).show();
                                    } catch (Exception e) {
                                        Toast.makeText(getApplicationContext(), "Download complete", Toast.LENGTH_LONG).show();
                                    }
                                    new Synchronise2().execute();
                                } else {
                                    //notify in firebase and admin notification also delete the file
                                    FirebaseFirestore fdb = FirebaseFirestore.getInstance();
                                    Map<String, Object> request = new HashMap<>();
                                    try {
                                        fdb.collection("failed_links").document(bookid.toString()).set(request);
                                    } catch(Exception ignored) {
                                    }
                                    try {
                                        Snackbar.make(findViewById(R.id.openbook), "Download link expired", Snackbar.LENGTH_LONG).show();
                                        AlertDialog.Builder builder = new AlertDialog.Builder(OpenBook.this);
                                        builder.setMessage("Download link has expired\nPlease resync and try again after some time");
                                        builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        });
                                        final AlertDialog alertDialog = builder.create();
                                        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                            @Override
                                            public void onShow(DialogInterface dialog) {
                                                alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));
                                            }
                                        });
                                        alertDialog.show();

                                    } catch (Exception e) {
                                        Toast.makeText(getApplicationContext(), "Download link has expired\nPlease resync and try again after some time", Toast.LENGTH_LONG).show();
                                    }
                                    //noinspection ResultOfMethodCallIgnored
                                    file.delete();
                                }
                            } else {
                                try {
                                    Snackbar.make(findViewById(R.id.openbook), "Download failed", Snackbar.LENGTH_LONG).show();
                                    downloadb.setClickable(true);
                                    downloadb.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));
                                    openb.setClickable(false);
                                    openb.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                                    deleteb.setClickable(false);
                                    deleteb.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                                } catch(Exception e) {
                                    Toast.makeText(getApplicationContext(), "Download failed", Toast.LENGTH_LONG).show();

                                }
                            }
                        } else {
                            try {
                                Snackbar.make(findViewById(R.id.openbook), "Download failed", Snackbar.LENGTH_LONG).show();
                                downloadb.setClickable(true);
                                downloadb.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));
                                openb.setClickable(false);
                                openb.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                                deleteb.setClickable(false);
                                deleteb.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                            } catch(Exception e) {
                                Toast.makeText(getApplicationContext(), "Download failed", Toast.LENGTH_LONG).show();

                            }
                        }
                    } else {
                        try {
                            Snackbar.make(findViewById(R.id.openbook), "Download failed", Snackbar.LENGTH_LONG).show();
                            downloadb.setClickable(true);
                            downloadb.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));
                            openb.setClickable(false);
                            openb.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                            deleteb.setClickable(false);
                            deleteb.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                        } catch(Exception e) {
                            Toast.makeText(getApplicationContext(), "Download failed", Toast.LENGTH_LONG).show();

                        }
                    }
                } else {
                    try {
                        Snackbar.make(findViewById(R.id.openbook), "Download failed", Snackbar.LENGTH_LONG).show();
                        downloadb.setClickable(true);
                        downloadb.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));
                        openb.setClickable(false);
                        openb.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                        deleteb.setClickable(false);
                        deleteb.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                    } catch(Exception e) {
                        Toast.makeText(getApplicationContext(), "Download failed", Toast.LENGTH_LONG).show();

                    }
                }
            }
        };
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
    public void openbook(View view) {
        Intent intent = new Intent(getApplicationContext(), PdfRead.class);
        intent.putExtra("bookid",bookid);
        startActivity(intent);
    }
    public void deletebook(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("ARE YOU SURE YOU WANT TO DELETE?");
        builder.setCancelable(true);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String filename = Objects.requireNonNull(getApplicationContext().getExternalFilesDir("")).getAbsolutePath() + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + bookid.toString();
                File file = new File(filename);
                boolean deleted = file.delete();
                if (deleted) {
                    Snackbar.make(findViewById(R.id.openbook), "Deleted", Snackbar.LENGTH_LONG).show();
                    new Synchronise3().execute();
                    new Synchronise1().execute();
                }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}