package com.android.project.elibrary;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.arch.persistence.room.Room;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    String[] recentbooks;
    Integer[] recentids;
    AlertDialog.Builder builder;
    AlertDialog alert;
    FirebaseFirestore fdb;
    int counter = 0;
    LocalData localData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SharedPreferences sharedPref1 = getSharedPreferences("Data",MODE_PRIVATE);
        fdb = FirebaseFirestore.getInstance();
        new Synchronise2().execute();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        final Menu menu = navigationView.getMenu();
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("Data",MODE_PRIVATE);
            Gson gson = new Gson();
            String json = sharedPreferences.getString("localData", "");

            localData = gson.fromJson(json, LocalData.class);
            int i = 0;
            String[] deptlist = localData.departments.keySet().toArray(new String[0]);
            Arrays.sort(deptlist);
            for (String depts : deptlist) {
                menu.add(R.id.deptgroup, Menu.NONE, i, depts.substring(4));
            }
        } catch(Exception ignored) {

        }
        if(!sharedPref1.contains("notfirsttime")) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = null;
            if (connectivityManager != null) {
                activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            }
            if (!(activeNetworkInfo != null && activeNetworkInfo.isConnected())) {
                builder = new AlertDialog.Builder(this);
                builder.setMessage("Please connect to the internet when using the app for the first time");
                builder.setCancelable(false);
                builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                alert = builder.create();
                alert.show();
            } else {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {

                }
                resync();
                SharedPreferences sharedPref2 = getSharedPreferences("Data",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref2.edit();
                editor.putBoolean("notfirsttime",true);
                editor.apply();
            }
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_update) {
            String url = "https://elibgithub.github.io/index";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
            return true;
        } else if(id == R.id.action_search) {
            onSearchRequested();
        } else if(id == R.id.admin) {

            Intent intent = new Intent(getApplicationContext(), Admin.class);
            startActivity(intent);
        } else if(id == R.id.resync) {
            ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = null;
            if (connectivityManager != null) {
                activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            }
            if(!(activeNetworkInfo!=null && activeNetworkInfo.isConnected())) {
                Snackbar.make(findViewById(R.id.openbook),"No Internet", Snackbar.LENGTH_LONG).show();
            } else {
                resync();
            }
        }

        return super.onOptionsItemSelected(item);
    }
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        new Synchronise2().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Synchronise2().execute();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId(), gid = item.getGroupId();
        Intent intent = new Intent(getApplicationContext(),SemesterList.class);
        if(gid == R.id.deptgroup) {
            intent.putExtra("department",item.getTitle());
            startActivity(intent);
            //Toast.makeText(getApplicationContext(),item.getTitle(),Toast.LENGTH_LONG).show();
        } else if (id == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,"Download E Library app from: https://elibgithub.github.io");
            sendIntent.setType("text/plain");
            Intent.createChooser(sendIntent,"Share via");
            startActivity(sendIntent);
        } else if (id == R.id.nav_feedback) {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            String Subject;
            Subject = "FEEDBACK";
            String mailto = "mailto:elib.feedback@gmail.com";
            emailIntent.putExtra(Intent.EXTRA_SUBJECT,Subject);
            emailIntent.setData(Uri.parse(mailto));
            try {
                startActivity(emailIntent);
            } catch (ActivityNotFoundException e) {
                Snackbar.make(findViewById(R.id.drawer_layout),"No suitable app found",Snackbar.LENGTH_LONG).show();

            }
        } else if (id == R.id.nav_suggestbook) {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            String Subject;
            Subject = "SUGGESTION";
            String mailto = "mailto:elib.feedback@gmail.com";
            emailIntent.putExtra(Intent.EXTRA_SUBJECT,Subject);
            emailIntent.setData(Uri.parse(mailto));
            try {
                startActivity(emailIntent);
            } catch (ActivityNotFoundException e) {
                Snackbar.make(findViewById(R.id.drawer_layout),"No suitable app found",Snackbar.LENGTH_LONG).show();
            }
        } else if (id == R.id.nav_bugreport) {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            String Subject;
            Subject = "BUG REPORT";
            String mailto = "mailto:elib.feedback@gmail.com";
            emailIntent.putExtra(Intent.EXTRA_SUBJECT,Subject);
            emailIntent.setData(Uri.parse(mailto));
            try {
                startActivity(emailIntent);
            } catch (ActivityNotFoundException e) {
                Snackbar.make(findViewById(R.id.drawer_layout),"No suitable app found",Snackbar.LENGTH_LONG).show();

            }
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @SuppressLint("StaticFieldLeak")
    private class Synchronise2 extends AsyncTask<Void ,Void,Integer>{
        @Override
        protected Integer doInBackground(Void... updates) {
            LocalDB localDB = Room.databaseBuilder(getApplicationContext(), LocalDB.class, "localDB").build();
            Download[] downloads = localDB.downloadDao().fetch_recents();
            recentbooks = new String[downloads.length];
            recentids = new Integer[downloads.length];
            int i = 0;
            for(Download download : downloads) {
                recentbooks[i] = download.name;
                recentids[i] = download.bookid;
                i++;
            }
            return 1;
        }
        @Override
        protected void onPostExecute(Integer n) {
            if(recentbooks.length > 0) {
                try {
                    ListAdapter adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, recentbooks);
                    ListView listView = findViewById(R.id.recentlist);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            Intent intent = new Intent(getApplicationContext(), PdfRead.class);
                            intent.putExtra("bookid", recentids[i]);
                            startActivity(intent);
                        }
                    });
                } catch(Exception e) {
                    //Do nothing
                }
            }
        }
    }

    void resync() {
        localData = new LocalData();
        builder = new AlertDialog.Builder(this);
        builder.setMessage("Synchronising...");
        ProgressBar progressBar = new ProgressBar(this,null, android.R.attr.progressBarStyle);
        builder.setView(progressBar);
        builder.setCancelable(false);
        alert = builder.create();
        alert.show();

        fdb.collection("maxCount").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    Integer maxCount = 0;
                    for(QueryDocumentSnapshot document: task.getResult()) {
                        maxCount = Integer.parseInt(document.getId());
                    }
                    new BusyWait().execute(maxCount);
                }
            }
        });
        fdb.collection("departments").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    Map<String, Department> departmentMap = new HashMap<>();
                    final ArrayList<String> fdepts = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        //traversing through every document in the collection
                        Department department = new Department();
                        department.name = document.getId();
                        departmentMap.put(document.getId(),department);
                        fdepts.add(department.name);
                    }
                    localData.departments = departmentMap;
                    for(final String fdept : fdepts) {
                        fdb.collection(fdept).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()) {
                                    Map<String, Semester> semesterMap = new HashMap<>();
                                    ArrayList<String> fsems = new ArrayList<>();
                                    for(QueryDocumentSnapshot document : task.getResult()) {
                                        Semester semester = new Semester();
                                        semester.name = document.getId();
                                        semesterMap.put(document.getId(), semester);
                                        fsems.add(semester.name);
                                    }
                                    Objects.requireNonNull(localData.departments.get(fdept)).semesters = semesterMap;
                                    for(final String fsem : fsems) {
                                        fdb.collection(fdept).document(fsem).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if(task.isSuccessful()) {
                                                    Map<String, Course> courseMap = new HashMap<>();
                                                    ArrayList<String> fcourses = new ArrayList<>();
                                                    Map<String, Object> map = task.getResult().getData();
                                                    for (final Map.Entry<String, Object> entry : Objects.requireNonNull(map).entrySet()) {
                                                        Course course = new Course();
                                                        course.name = entry.getKey();
                                                        course.courseid = entry.getValue().toString();
                                                        courseMap.put(course.courseid,course);
                                                        fcourses.add(course.courseid);
                                                    }
                                                    Objects.requireNonNull(Objects.requireNonNull(localData.departments.get(fdept)).semesters.get(fsem)).courses = courseMap;
                                                    for(final String fcourse : fcourses) {
                                                        fdb.collection(fdept).document(fsem).collection(fcourse).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                if(task.isSuccessful()) {
                                                                    Map<String, Book> bookMap = new HashMap<>();
                                                                    for(QueryDocumentSnapshot document : task.getResult()) {
                                                                        Book book = new Book();
                                                                        book.bookid = Integer.parseInt(document.getId());
                                                                        book.link = document.getString("link");
                                                                        book.name = document.getString("name");
                                                                        bookMap.put(document.getId(),book);
                                                                        counter++;
                                                                        //Toast.makeText(getApplicationContext(),Integer.valueOf(counter).toString(),Toast.LENGTH_SHORT).show();
                                                                    }
                                                                    Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(localData.departments.get(fdept)).semesters.get(fsem)).courses.get(fcourse)).books = bookMap;
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }
    @SuppressLint("StaticFieldLeak")
    private class BusyWait extends AsyncTask<Integer ,Void,Integer>{
        @Override
        protected Integer doInBackground(Integer... data) {
            while(counter < data[0]) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ignored) {

                }
            }
            return 1;
        }
        @Override
        protected void onPostExecute(Integer n) {
            alert.cancel();
            SharedPreferences sharedPreferences = getSharedPreferences("Data",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();
            String json = gson.toJson(localData);
            editor.putString("localData",json);
            editor.apply();
            Toast.makeText(getApplicationContext(),"Database updated", Toast.LENGTH_LONG).show();
            try {
                Snackbar.make(findViewById(R.id.drawer_layout), "Database updated", Snackbar.LENGTH_LONG).show();

            } catch(Exception e) {
                Toast.makeText(getApplicationContext(),"Database updated", Toast.LENGTH_LONG).show();
            }
            recreate();
        }
    }
}
