package com.ai180183.johortravelrouteplanner;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import com.ai180183.johortravelrouteplanner.classes.AttractivePlaces;
import com.ai180183.johortravelrouteplanner.classes.Users;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class CategoryStay extends AppCompatActivity {
    private static final String TAG = "CategoryStay";
    private RecyclerView stayRV;
    private PageAdapterEat stayAdapter;
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private ListenerRegistration fListener;

    EditText editLogTitle, editLogContent;
    TextView logTitle, logContent;
    ImageView editLogImage;
    Button saveBtn, uploadBtn;
    StorageReference Folder;
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseUser fUser = fAuth.getCurrentUser();
    String userID;
    String userEmail;
    Users user = new Users();
    FloatingActionButton fab;

    ArrayList<AttractivePlaces> placesArrayList;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_stay);

        Folder = FirebaseStorage.getInstance().getReference().child("AttractivePlaces");
        stayRV = findViewById(R.id.stayListView);
        editLogTitle = findViewById(R.id.edit_log_title);
        editLogContent = findViewById(R.id.edit_log_description);
        editLogImage = findViewById(R.id.edit_log_image);
        logTitle = findViewById(R.id.log_title);
        logContent = findViewById(R.id.log_description);
        saveBtn = findViewById(R.id.saveTravelLog);
        uploadBtn = findViewById(R.id.uploadTravelLog);

        placesArrayList = new ArrayList<>();
        stayRV.setHasFixedSize(true);
        stayRV.setLayoutManager(new LinearLayoutManager(this));
        stayAdapter = new PageAdapterEat(placesArrayList, getApplicationContext(), fStore);
        stayRV.setAdapter(stayAdapter);

        // calling the action bar
        ActionBar actionBar = getSupportActionBar();
        // showing the back button in action bar
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        loadLogList();
        fListener = fStore.collection("attractivePlaces")
                .orderBy("placeDate", Query.Direction.DESCENDING)
                .whereEqualTo("category", "Category Stay")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e(TAG, "Listen failed!", error);
                            return;
                        }

                        ArrayList<AttractivePlaces> logList = new ArrayList<>();

                        for (DocumentSnapshot doc : value) {

                            AttractivePlaces places = doc.toObject(AttractivePlaces.class);
                            places.setPlaceID(doc.getString("placeID"));
                            logList.add(places);
                        }

                        stayAdapter.notifyDataSetChanged();
                    }
                });

        fab = (FloatingActionButton) findViewById(R.id.fabAdd);
        if (fUser != null) {
            userEmail = fUser.getEmail();
        }
        if (userEmail!=null && !userEmail.equals("huifenmok@gmail.com")) {
            fab.setVisibility(View.GONE);
        } else {
            fab.setVisibility(View.VISIBLE);
        }
        fab.setTooltipText("Add a new place");
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CategoryStay.this, AddNewPlace.class));
                finish();
            }
        });

    }

    private void loadLogList() {
        fStore.collection("attractivePlaces")
                .orderBy("placeDate", Query.Direction.DESCENDING)
                .whereEqualTo("category", "Category Stay")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc : task.getResult()) {

                                AttractivePlaces places = doc.toObject(AttractivePlaces.class);
                                places.setPlaceID(doc.getString("placeID"));
                                placesArrayList.add(places);
                            }

                            stayAdapter = new PageAdapterEat(placesArrayList, getApplicationContext(), fStore);
                            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                            stayRV.setLayoutManager(mLayoutManager);
                            stayRV.setItemAnimator(new DefaultItemAnimator());
                            stayRV.setAdapter(stayAdapter);
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
        if (fUser != null) {
            userID = fUser.getUid();
        }
        fStore.collection("users")
                .document(userID)
                .addSnapshotListener(CategoryStay.this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value.exists()) {
                            user.setFullName(value.getString("name"));
                            user.setEmail(value.getString("email"));
                            user.setPassword(value.getString("password"));
                            user.setPhone(value.getString("phone"));
                            user.setUser(value.getBoolean("isUser"));
                            user.setImgUrl(value.getString("profileImage"));
                            user.setUserId(value.getString("userID"));
                            if (user.getUser()) {
                                fab.setVisibility(View.GONE);
                            }
                        }
                    }
                });
    }

    // this event will enable the back function to the button on press
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(getApplicationContext(), ExploreActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
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
        ArrayList<AttractivePlaces> filteredList = new ArrayList<>();

        for (AttractivePlaces item : placesArrayList) {
            if (item.getPlaceName().toLowerCase().contains(text.toLowerCase())
                    || item.getPlaceDesc().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No Data Found..", Toast.LENGTH_SHORT).show();
        } else {
            stayAdapter.filterList(filteredList);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"on Start");
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