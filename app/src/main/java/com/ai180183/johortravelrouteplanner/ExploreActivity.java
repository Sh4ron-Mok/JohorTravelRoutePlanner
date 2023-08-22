package com.ai180183.johortravelrouteplanner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.ai180183.johortravelrouteplanner.classes.FeaturedLocation;
import com.ai180183.johortravelrouteplanner.classes.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ExploreActivity extends AppCompatActivity {
    private static final String TAG = "Explore";
    com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigation;
    Button eat, play, stay, chooseBtn;
    TextView mCreateBtn;
    ImageView slideShow;
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseUser fUser = fAuth.getCurrentUser();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    Users user;
    String userID;
    private ProgressDialog progressDialog;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        mCreateBtn = findViewById(R.id.createBtn);
        user = new Users();

        eat = findViewById(R.id.eat);
        play = findViewById(R.id.play);
        stay = findViewById(R.id.stay);
        chooseBtn = findViewById(R.id.chooseBtn);
        slideShow = findViewById(R.id.slideShow);

        if(fUser == null){
            mCreateBtn.setVisibility(View.VISIBLE);
            fAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        mCreateBtn.setText("Welcome.");
                        loadImages();
                    }
                }
            });
            mCreateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent linkAcc = new Intent(ExploreActivity.this,RegisterActivity.class);
                    startActivity(linkAcc);
                    finish();
                }
            });
        } else {
            loadImages();
            userID = fUser.getUid();
            DocumentReference docRef = fStore.collection("users").document(userID);
            docRef.addSnapshotListener(ExploreActivity.this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@NonNull DocumentSnapshot documentSnapshot, @NonNull FirebaseFirestoreException e) {
                    if (documentSnapshot.exists()) {
                        user.setFullName(documentSnapshot.getString("name"));
                        user.setUser(documentSnapshot.getBoolean("isUser"));
                        Log.d(TAG,"onSuccess: Username is "+user.getFullName()+", isUser: "+user.getUser());
                        if (!user.getUser()) {
                            chooseBtn.setVisibility(View.VISIBLE);
                        }
                    }
                    mCreateBtn.setVisibility(View.VISIBLE);
                    if (user.getFullName() != null) {
                        mCreateBtn.setText("Welcome, " + user.getFullName());
                        mCreateBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(getApplicationContext(),AccountActivity.class));
                                finish();
                            }
                        });
                    } else {
                        mCreateBtn.setText("Welcome, Traveller.");
                        mCreateBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent linkAcc = new Intent(getApplicationContext(),RegisterActivity.class);
                                startActivity(linkAcc);
                                finish();
                            }
                        });
                    }
                }
            });
        }

        progressDialog = new ProgressDialog(ExploreActivity.this);
        progressDialog.setMessage("Uploading images, please wait...");
        chooseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent,1000);
            }
        });

        eat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ExploreActivity.this, CategoryEat.class));
                finish();
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ExploreActivity.this, CategoryPlay.class));
                finish();
            }
        });

        stay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ExploreActivity.this, CategoryStay.class));
                finish();
            }
        });

        BottomNavigationView bottomNavigationView;
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_explore);

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener( new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_explore:
                        Intent intent1 = new Intent(ExploreActivity.this,ExploreActivity.class);
                        startActivity(intent1);
                        finish();
                        return true;
                    case R.id.navigation_plan:
                        if (user.getFullName() == null) {
                            Toast.makeText(getApplicationContext(), "Please register account first. ", Toast.LENGTH_SHORT).show();
                            Intent linkAcc = new Intent(ExploreActivity.this,RegisterActivity.class);
                            startActivity(linkAcc);
                            finish();
                        } else {
                            Intent intent2 = new Intent(ExploreActivity.this,PlanActivity.class);
                            startActivity(intent2);
                            finish();
                        }
                        return true;
                    case R.id.navigation_log:
                        if (user.getFullName() == null) {
                            Toast.makeText(getApplicationContext(), "Please register account first. ", Toast.LENGTH_SHORT).show();
                            Intent linkAcc = new Intent(getApplicationContext(),RegisterActivity.class);
                            startActivity(linkAcc);
                            finish();
                        } else {
                            Intent intent3 = new Intent(ExploreActivity.this, TravelLogActivity.class);
                            startActivity(intent3);
                            finish();
                        }
                        return true;
                    case R.id.navigation_account:
                        if (user.getFullName() == null) {
                            Toast.makeText(getApplicationContext(), "Please register account first. ", Toast.LENGTH_SHORT).show();
                            Intent linkAcc = new Intent(ExploreActivity.this,RegisterActivity.class);
                            startActivity(linkAcc);
                            finish();
                        } else {
                            Intent intent4 = new Intent(ExploreActivity.this,AccountActivity.class);
                            startActivity(intent4);
                            finish();
                        }
                        return true;
                }
                return false;
            }
        });

        //make translucent statusBar on kitkat devices
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        //make fully Android Transparent Status bar
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000){
            if(resultCode == Activity.RESULT_OK && data != null){
                Uri imageUri = data.getData();
                Picasso.get()
                        .load(imageUri)
                        .into(slideShow);
                progressDialog.show();
                uploadImageToFirebase(imageUri);
            }
        } else {
            Toast.makeText(this, "Error, Please try again", Toast.LENGTH_SHORT).show();
            Log.d(TAG,"onFailed：" + Exception.class.toString());
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        // upload image to firebase storage
        final StorageReference image = storageReference.child("ImageFolder/image.jpg");
        image.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d("tag", "onSuccess: Uploaded Image URl is " + uri.toString());
                        Picasso.get().load(uri).into(slideShow);
                        DocumentReference docRef = fStore.collection("featuredLocation").document("image");
                        docRef.update("ImgLink", uri.toString());
                        progressDialog.dismiss();
                    }
                });

                Toast.makeText(ExploreActivity.this, "Image Is Uploaded.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "OnFailure: Upload failed, "+e.toString());
                Toast.makeText(ExploreActivity.this, "Upload Failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadImages() {
        DocumentReference docRef = fStore.collection("featuredLocation").document("image");
        docRef.addSnapshotListener(ExploreActivity.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Picasso.get()
                            .load(documentSnapshot.getString("ImgLink"))
                            .into(slideShow);
                } else {
                    Log.e(TAG, "onFailed: Documents do not exists, " + e.toString());
                }
            }
        });
    }

    public static void setWindowFlag(ExploreActivity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = ((Window) win).getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
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

