package com.ai180183.johortravelrouteplanner;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.ai180183.johortravelrouteplanner.classes.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

public class AddNewAdmin extends AppCompatActivity {
    private static final String TAG = "AddNewAdmin";
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    Button admins, users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_admin);

        // calling the action bar
        ActionBar actionBar = getSupportActionBar();
        // showing the back button in action bar
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        admins = findViewById(R.id.changeAdmin);
        users = findViewById(R.id.changeUser);

        admins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ChangeAdmin.class));
                finish();
            }
        });

        users.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ChangeUsers.class));
                finish();
            }
        });
    }

    // this event will enable the back function to the button on press
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(getApplicationContext(), AccountActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
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