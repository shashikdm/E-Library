package com.android.project.elibrary;

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

import java.util.Arrays;
import java.util.Objects;

public class SemesterList extends AppCompatActivity {
    String[] semesters;
    LocalData localData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_semester_list);
        final String department = Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("department")).toString();
        TextView textview = findViewById(R.id.deptname);
        textview.setText(department.toUpperCase());
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("Data",MODE_PRIVATE);
            Gson gson = new Gson();
            String json = sharedPreferences.getString("localData", "");
            localData = gson.fromJson(json, LocalData.class);
            semesters = Objects.requireNonNull(localData.departments.get("DEPT" + department)).semesters.keySet().toArray(new String[0]);
            Arrays.sort(semesters);
            ListAdapter adapter = new ArrayAdapter<>(SemesterList.this, android.R.layout.simple_list_item_1, semesters);
            ListView listView = findViewById(R.id.semlist);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(getApplicationContext(),CourseList.class);
                    intent.putExtra("department", department);
                    intent.putExtra("semester",semesters[i]);
                    startActivity(intent);
                }
            });
        } catch(Exception e) {
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
        }
    }
}
