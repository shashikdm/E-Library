package com.android.project.elibrary;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Objects;

public class CourseList extends AppCompatActivity {
    ArrayList<String> courselist = new ArrayList<>();
   ArrayList<String> courses = new ArrayList<>();
    String department;
    String semester;
    LocalData localData;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_semester_list);
        department = Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("department")).toString();
        semester = Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("semester")).toString();
        TextView textview = findViewById(R.id.deptname);
        textview.setText(department.toUpperCase()+" > " + semester);
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("Data",MODE_PRIVATE);
            Gson gson = new Gson();
            String json = sharedPreferences.getString("localData", "");
            localData = gson.fromJson(json, LocalData.class);
            for(Course dcourse : Objects.requireNonNull(Objects.requireNonNull(localData.departments.get("DEPT" + department)).semesters.get(semester)).courses.values()) {
                courselist.add(dcourse.name);
                courses.add(dcourse.courseid);
            }
            ListAdapter adapter = new ArrayAdapter<>(CourseList.this, android.R.layout.simple_list_item_1, courselist);
            ListView listView = findViewById(R.id.semlist);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(getApplicationContext(),BookList.class);
                    intent.putExtra("department", department);
                    intent.putExtra("semester",semester);
                    intent.putExtra("courseid",courses.get(i));
                    startActivity(intent);
                }
            });
        } catch(Exception e) {
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
        }
    }
}
