package com.ai180183.johortravelrouteplanner;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ai180183.johortravelrouteplanner.classes.TravelLog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class TravelLogActivity extends AppCompatActivity {
    com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigation;
    private static final String TAG = "TravelLog";
    private RecyclerView logRV;
    private PageAdapterTravelLog logAdapter;
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private ListenerRegistration fListener;

    EditText editLogTitle, editLogContent;
    TextView logTitle, logContent;
    ImageView editLogImage;
    Button saveBtn, uploadBtn;
    StorageReference Folder;
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    ArrayList<TravelLog> logArrayList;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_log);

        Folder = FirebaseStorage.getInstance().getReference().child("ImageFolder");
        logRV = findViewById(R.id.rvTravelLog);

        editLogTitle = findViewById(R.id.edit_log_title);
        editLogContent = findViewById(R.id.edit_log_description);
        editLogImage = findViewById(R.id.edit_log_image);
        logTitle = findViewById(R.id.log_title);
        logContent = findViewById(R.id.log_description);
        saveBtn = findViewById(R.id.saveTravelLog);
        uploadBtn = findViewById(R.id.uploadTravelLog);

        logArrayList = new ArrayList<>();
        logRV.setHasFixedSize(true);
        logRV.setLayoutManager(new LinearLayoutManager(this));
        if (fAuth.getCurrentUser().getEmail()!=null) {
            logAdapter = new PageAdapterTravelLog(logArrayList, getApplicationContext(), fStore);
            logRV.setAdapter(logAdapter);
        }

        loadLogList();
        fListener = fStore.collection("travelLog")
                .orderBy("logDate", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e(TAG, "Listen failed!", error);
                            return;
                        }

                        ArrayList<TravelLog> logList = new ArrayList<>();

                        for (DocumentSnapshot doc : value) {
                            TravelLog log = doc.toObject(TravelLog.class);
                            log.setLogID(doc.getId());
                            log.setUserEmail(doc.getString("userEmail"));
                            logList.add(log);
                        }

                        logAdapter.notifyDataSetChanged();
                    }
                });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabAdd);
        fab.setTooltipText("Add a new travel log");
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TravelLogActivity.this, AddTravelLog.class));
                finish();
            }
        });

        BottomNavigationView bottomNavigationView;
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_log);

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_explore:
                        Intent intent1 = new Intent(TravelLogActivity.this, ExploreActivity.class);
                        startActivity(intent1);
                        finish();
                        return true;
                    case R.id.navigation_plan:
                        Intent intent2 = new Intent(TravelLogActivity.this, PlanActivity.class);
                        startActivity(intent2);
                        finish();
                        return true;
                    case R.id.navigation_log:
                        Intent intent3 = new Intent(TravelLogActivity.this, TravelLogActivity.class);
                        startActivity(intent3);
                        finish();
                        return true;
                    case R.id.navigation_account:
                        Intent intent4 = new Intent(TravelLogActivity.this, AccountActivity.class);
                        startActivity(intent4);
                        finish();
                        return true;
                }
                return false;
            }
        });
    }

    private void loadLogList() {
        fStore.collection("travelLog")
                .orderBy("logDate", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc : task.getResult()) {
                                TravelLog log = doc.toObject(TravelLog.class);
                                log.setLogID(doc.getId());
                                logArrayList.add(log);
                            }

                            logAdapter = new PageAdapterTravelLog(logArrayList, getApplicationContext(), fStore);
                            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                            logRV.setLayoutManager(mLayoutManager);
                            logRV.setItemAnimator(new DefaultItemAnimator());
                            logRV.setAdapter(logAdapter);
                        } else {
                            Log.e(TAG, "OnFailure: Error getting documents: ", task.getException());
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG,"OnFailure: Cannot load data, "+e.toString());
            }
        });
    }

    // calling on create option menu layout to inflate the menu file.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.search_travellog, menu);
        MenuItem searchItem = menu.findItem(R.id.searchTravelLog);

        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }
        });
        return true;
    }

    private void filter(String text) {
        ArrayList<TravelLog> filteredList = new ArrayList<>();

        for (TravelLog item : logArrayList) {
            if (item.getLogTitle().toLowerCase().contains(text.toLowerCase())
                    || item.getLogContent().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No Data Found..", Toast.LENGTH_SHORT).show();
        } else {
            logAdapter.filterList(filteredList);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"on Start");
        if (fAuth.getCurrentUser() == null) {
            startActivity(new Intent(TravelLogActivity.this, RegisterActivity.class));
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"on Resume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"on Pause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"on Stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fListener.remove();
        Log.d(TAG,"on Destroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG,"on Restart");
    }
}