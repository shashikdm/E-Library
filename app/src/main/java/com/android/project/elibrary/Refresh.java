package com.android.project.elibrary;

import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Refresh extends AppCompatActivity {
    final ArrayList<Book> finalBooks = new ArrayList<>();
    final FirebaseFirestore fdb = FirebaseFirestore.getInstance();
    TextView textView;
    QueryDocumentSnapshot sem;
    String filename;
    File file;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refresh);
        textView = findViewById(R.id.details);
        textView.setMovementMethod(new ScrollingMovementMethod());
        filename = Objects.requireNonNull(getApplicationContext().getExternalFilesDir("")).getAbsolutePath() + File.separator + "Log_" + Calendar.getInstance().getTime().toString()+".txt";
        file = new File(filename);
        try {
            file.createNewFile();
            FileOutputStream fileOuputStream = new FileOutputStream(filename);
            fileOuputStream.write(Calendar.getInstance().getTime().toString().getBytes());
            fileOuputStream.write("\n".getBytes());
            fileOuputStream.close();
        } catch (FileNotFoundException e) {
            Snackbar.make(findViewById(R.id.refresh), e.toString(), Snackbar.LENGTH_LONG).show();
        } catch (IOException e) {
            Snackbar.make(findViewById(R.id.refresh), e.toString(), Snackbar.LENGTH_LONG).show();
        }
        textView.append("Begin\n");
        fdb.collection("DEPTCSE").document("SEMESTER II").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                fdb.collection("DEPTMEC").document("SEMESTER II").set(documentSnapshot.getData());
                fdb.collection("DEPTMEC").document("SEMESTER II").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(final DocumentSnapshot documentSnapshot) {
                        Map<String, Object> map = documentSnapshot.getData();
                        for (final Map.Entry<String, Object> entry : Objects.requireNonNull(map).entrySet()) {
                            fdb.collection("DEPTCSE").document("SEMESTER II").collection(entry.getValue().toString()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    for (QueryDocumentSnapshot documentSnapshot1 : queryDocumentSnapshots) {
                                        fdb.collection("DEPTMEC").document("SEMESTER II").collection(entry.getValue().toString()).document(documentSnapshot1.getId()).set(documentSnapshot1.getData());
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });
        /*fdb.collection("DEPTCSE").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {//FROM
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {//TO
                for(QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots) {
                    if(documentSnapshot.getId().equals("SEMESTER II")) {
                        fdb.collection("DEPTECE").document("SEMESTER II").set(documentSnapshot.getData());
                        fdb.collection("DEPTECE").document("SEMESTER II").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                                Map<String, Object> map = documentSnapshot.getData();
                                for (final Map.Entry<String, Object> entry : Objects.requireNonNull(map).entrySet()) {
                                    fdb.collection("DEPTCSE").document("SEMESTER II").collection(entry.getValue().toString()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            for (QueryDocumentSnapshot documentSnapshot1 : queryDocumentSnapshots) {
                                                fdb.collection("DEPTECE").document("SEMESTER II").collection(entry.getValue().toString()).document(documentSnapshot1.getId()).set(documentSnapshot1.getData());
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            }
        });*/
        /*fdb.collection("departments").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    for (final QueryDocumentSnapshot dept : task.getResult()) {
                        //traversing through every document in the collection
                        fdb.collection(dept.getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()) {
                                    for (QueryDocumentSnapshot semtemp : task.getResult()) {
                                        sem = semtemp;
                                        fdb.collection(dept.getId()).document(sem.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if(task.isSuccessful()) {
                                                    Map<String, Object> map = task.getResult().getData();
                                                    for (final Map.Entry<String, Object> entry : Objects.requireNonNull(map).entrySet()) {
                                                        //entry.getkey contains all the courseids
                                                        fdb.collection(dept.getId()).document(sem.getId()).collection(entry.getValue().toString()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                if(task.isSuccessful()) {
                                                                    //through every book
                                                                    ArrayList<QueryDocumentSnapshot> books = new ArrayList<>();
                                                                    for(QueryDocumentSnapshot book : task.getResult()) {
                                                                        books.add(book);
                                                                    }
                                                                    Synchronise1 synchronise1 = new Synchronise1();
                                                                    synchronise1.dept = dept.getId();
                                                                    synchronise1.course = entry.getValue().toString();
                                                                    synchronise1.innersem = sem.getId();
                                                                    synchronise1.execute(books);
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
        });*/
        fdb.collection("departments").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    final ArrayList<String> fdepts = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        //traversing through every document in the collection
                        fdepts.add(document.getId());
                    }
                    for(final String fdept : fdepts) {
                        fdb.collection(fdept).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()) {
                                    ArrayList<String> fsems = new ArrayList<>();
                                    for(QueryDocumentSnapshot document : task.getResult()) {
                                        fsems.add(document.getId());
                                    }
                                    for(final String fsem : fsems) {
                                        fdb.collection(fdept).document(fsem).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if(task.isSuccessful()) {
                                                    ArrayList<String> fcourses = new ArrayList<>();
                                                    Map<String, Object> map = task.getResult().getData();
                                                    for (final Map.Entry<String, Object> entry : Objects.requireNonNull(map).entrySet()) {
                                                        fcourses.add(entry.getValue().toString());
                                                    }
                                                    for(final String fcourse : fcourses) {
                                                        fdb.collection(fdept).document(fsem).collection(fcourse).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                if(task.isSuccessful()) {
                                                                    ArrayList<QueryDocumentSnapshot> books = new ArrayList<>();
                                                                    for(QueryDocumentSnapshot book : task.getResult()) {
                                                                        books.add(book);
                                                                    }
                                                                    Synchronise1 synchronise1 = new Synchronise1();
                                                                    synchronise1.dept = fdept;
                                                                    synchronise1.course = fcourse;
                                                                    synchronise1.innersem = fsem;
                                                                    synchronise1.execute(books);
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
    Boolean flag1 = false, flag2 = false, flag3 = false;
    @SuppressLint("StaticFieldLeak")
    private class Synchronise1 extends AsyncTask<ArrayList<QueryDocumentSnapshot>,String,Integer> {
        public String dept, course, innersem;
        @Override
        protected Integer doInBackground(ArrayList<QueryDocumentSnapshot>... books) {
            for(QueryDocumentSnapshot book: books[0]) {
                publishProgress(dept+">"+innersem+">"+course+">"+book.getId()+": ");
                Document document;
                String newlink, url;
                //Create url
                url = "http://www.mediafire.com/file/";
                int pos, slashcount = 0;
                for (pos = 0; slashcount < 4; pos++) {
                    if (book.getString("link").charAt(pos) == '/') {
                        slashcount++;
                    }
                }
                url = url + book.getString("link").substring(pos);
                try {
                    document = Jsoup.connect(url).userAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:61.0) Gecko/20100101 Firefox/61.0").get();
                    try {
                        Element linkelement = document.select("div[class=download_link]").first();
                        linkelement = linkelement.child(1);
                        newlink = linkelement.attributes().get("href");
                        newlink = "https://" + newlink.substring(7);
                        //Toast.makeText(getApplicationContext(),newlink,Toast.LENGTH_LONG).show();
                        if (newlink.substring(0, 16).equals("https://download")) {
                            fdb.collection(dept).document(innersem).collection(course).document(book.getId()).update("link",newlink);
                            fdb.collection("failed_links").document(book.getId()).delete();
                            //Toast.makeText(getApplicationContext(), newlink, Toast.LENGTH_LONG).show();
                            //todo succesully obtained link
                            publishProgress("Successfully updated\n");
                        } else {
                            publishProgress("New link invalid\n");

                        }
                    } catch (Exception e) {
                        //todo will occur if they change webpage format
                        //Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
                        publishProgress("New link not found\n");

                    }
                } catch (IOException e) {
                    //todo comes here due to copystrike
                    publishProgress("Copyright claim\n");
                }
            }
            return 1;
        }

        @Override
        protected void onProgressUpdate(String... text) {
            textView.append(text[0]);
            try {
                FileOutputStream fileOuputStream = new FileOutputStream(filename,true);
                fileOuputStream.write(text[0].getBytes());
                fileOuputStream.close();
            } catch (FileNotFoundException e) {
                //Do nothing
            } catch (IOException e) {
                //Do nothing
            }
        }
        @Override
        protected void onPostExecute(Integer n) {

        }
    }
}
