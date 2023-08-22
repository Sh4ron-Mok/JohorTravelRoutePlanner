package com.ai180183.johortravelrouteplanner;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ai180183.johortravelrouteplanner.classes.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Objects;

public class ChangeAdmin extends AppCompatActivity {
    private static final String TAG = "ChangeAdmin";
    private RecyclerView userRV;
    private PageAdapterAdmin userAdapter;
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private ListenerRegistration fListener;

    StorageReference Folder;
    TextView name;
    ImageView profileImage;
    Button changeUser;
    FirebaseAuth fAuth = FirebaseAuth.getInstance();

    ArrayList<Users> userArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_users);

        Folder = FirebaseStorage.getInstance().getReference().child("ImageFolder");
        userRV = findViewById(R.id.usersRView);
        name = findViewById(R.id.listViewText);
        profileImage = findViewById(R.id.listViewImg);
        changeUser = findViewById(R.id.upgradeUser);

        userArrayList = new ArrayList<>();
        userRV.setHasFixedSize(true);
        userRV.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new PageAdapterAdmin(userArrayList, getApplicationContext(), fStore);
        userRV.setAdapter(userAdapter);

        // calling the action bar
        ActionBar actionBar = getSupportActionBar();
        // showing the back button in action bar
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        loadLogList();

        fListener = fStore.collection("users")
                .whereEqualTo("isUser",false)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e(TAG, "Listen failed!", error);
                            return;
                        }

                        ArrayList<Users> userList = new ArrayList<>();

                        assert value != null;
                        for (DocumentSnapshot doc : value) {
                            Users users = doc.toObject(Users.class);
                            assert users != null;
                            users.setFullName(doc.getString("name"));
                            users.setImgUrl(doc.getString("profileImage"));
                            users.setUserId(Objects.requireNonNull(fAuth.getCurrentUser()).getUid());
                            userList.add(users);
                        }
                        userAdapter.notifyDataSetChanged();
                    }
                });

    }

    private void loadLogList() {
        fStore.collection("users")
                .whereEqualTo("isUser",false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc : task.getResult()) {
                                Users users = doc.toObject(Users.class);
                                users.setUserId(doc.getString("userID"));
                                users.setFullName(doc.getString("name"));
                                users.setImgUrl(doc.getString("profileImage"));
                                Log.d(TAG, "userID => "+users.getUserId());
                                userArrayList.add(users);
                            }

                            userAdapter = new PageAdapterAdmin(userArrayList, getApplicationContext(), fStore);
                            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                            userRV.setLayoutManager(mLayoutManager);
                            userRV.setItemAnimator(new DefaultItemAnimator());
                            userRV.setAdapter(userAdapter);
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

    // this event will enable the back function to the button on press
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(getApplicationContext(), AddNewAdmin.class));
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
        ArrayList<Users> filteredList = new ArrayList<>();

        for (Users item : userArrayList) {
            if (item.getFullName().toLowerCase().contains(text.toLowerCase()) || item.getFullName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No Data Found..", Toast.LENGTH_SHORT).show();
        } else {
            userAdapter.filterList(filteredList);
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
        Log.d(TAG,"on Destroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG,"on Restart");
    }
}