package com.android.project.elibrary;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

public class SearchableActivity extends ListActivity {
    ArrayList<String> bdept = new ArrayList<>();
    ArrayList<String> bsem = new ArrayList<>();
    ArrayList<String> bcourse = new ArrayList<>();
    ArrayList<String> bid = new ArrayList<>();
    ArrayList<String> bname = new ArrayList<>();
    ArrayList<String> blink = new ArrayList<>();
    AlertDialog.Builder builder;
    AlertDialog alert;
    LocalData localData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

            final String query = intent.getStringExtra(SearchManager.QUERY);
            builder = new AlertDialog.Builder(SearchableActivity.this);
            builder.setMessage("Searching...");
            ProgressBar progressBar = new ProgressBar(SearchableActivity.this,null, android.R.attr.progressBarStyle);
            builder.setView(progressBar);
            builder.setCancelable(false);
            alert = builder.create();
            alert.show();
            try {
                SharedPreferences sharedPreferences = getSharedPreferences("Data",MODE_PRIVATE);
                Gson gson = new Gson();
                String json = sharedPreferences.getString("localData", "");

                localData = gson.fromJson(json, LocalData.class);
                for(Department department : localData.departments.values()) {
                    for(Semester semester : department.semesters.values()) {
                        for(Course course : semester.courses.values()) {
                            for(Book book : course.books.values()) {
                                if(book.name.toUpperCase().contains(query.toUpperCase()) && !bid.contains(Integer.valueOf(book.bookid).toString())) {
                                    bname.add(book.name);
                                    bdept.add(department.name.substring(4));
                                    bsem.add(semester.name);
                                    bcourse.add(course.courseid);
                                    blink.add(book.link);
                                    bid.add(Integer.valueOf(book.bookid).toString());
                                }
                            }
                        }
                    }
                }
                alert.cancel();
                if(bname.isEmpty()) {
                    Toast.makeText(getApplicationContext(),"No result", Toast.LENGTH_LONG).show();
                    finish();
                }
                ListAdapter adapter;
                try {
                    adapter = new ArrayAdapter<>(SearchableActivity.this, android.R.layout.simple_list_item_1, bname);
                    setListAdapter(adapter);
                } catch(Exception ignored) {
                }
            } catch(Exception e) {
                alert.cancel();
            }
        }
    }
    protected void onListItemClick (ListView l,
                                    View v,
                                    int position,
                                    long id) {
        try {
            Intent intent = new Intent(getApplicationContext(), OpenBook.class);
            intent.putExtra("bookid", bid.get(position));
            intent.putExtra("name", bname.get(position));
            intent.putExtra("department", bdept.get(position));
            intent.putExtra("semester", bsem.get(position));
            intent.putExtra("courseid", bcourse.get(position));
            intent.putExtra("link",blink.get(position));
            startActivity(intent);
        } catch(Exception e) {
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
        }
    }
}
