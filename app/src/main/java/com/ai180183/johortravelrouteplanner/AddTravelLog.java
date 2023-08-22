package com.ai180183.johortravelrouteplanner;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.ai180183.johortravelrouteplanner.classes.TravelLog;
import com.ai180183.johortravelrouteplanner.classes.Users;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddTravelLog extends AppCompatActivity {
    EditText logTitleEdt, logContentEdt;
    ImageView logImgEdt;
    Button saveLogBtn, uploadImgBtn;
    StorageReference Folder;
    ProgressDialog progressDialog;

    FirebaseUser firebaseUser;
    FirebaseFirestore fStore;
    String userID;
    String userEmail;
    String logID;
    private TravelLog tLog;
    private final Users user = new Users();
    private static final String TAG = "AddTravelLog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_travel_log);
        fStore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userEmail = firebaseUser.getEmail();
        userID = firebaseUser.getUid();
        logID = fStore.collection("travelLog").document().getId();

        logImgEdt = findViewById(R.id.edit_log_image);
        logTitleEdt = findViewById(R.id.edit_log_title);
        logContentEdt = findViewById(R.id.edit_log_description);
        saveLogBtn = findViewById(R.id.saveTravelLog);
        uploadImgBtn = findViewById(R.id.uploadTravelLog);
        Folder = FirebaseStorage.getInstance().getReference();

        tLog = new TravelLog();

        // calling the action bar
        ActionBar actionBar = getSupportActionBar();
        // showing the back button in action bar
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(AddTravelLog.this);
        progressDialog.setMessage("Uploading image, please wait...");

        getUserProfile();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            tLog.setLogID(bundle.getString("UpdateLogId"));
            tLog.setLogTitle(bundle.getString("UpdateLogTitle"));
            tLog.setLogContent(bundle.getString("UpdateLogContent"));
            tLog.setImgUrl(bundle.getString("UpdateLogImg"));
            tLog.setLogDate((Date) bundle.getSerializable("UpdateLogDate"));
            tLog.setUserEmail(bundle.getString("UpdateUserEmail"));
            tLog.setUser(bundle.getBoolean("UpdateUserType"));

            logTitleEdt.setText(tLog.getLogTitle());
            logContentEdt.setText(tLog.getLogContent());
            Picasso.get().load(tLog.getImgUrl()).into(logImgEdt);
        }

        saveLogBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                String title = logTitleEdt.getText().toString();
                String content = logContentEdt.getText().toString();
                tLog.setUser(user.getUser());
                Log.d(TAG,"isUser: "+tLog.getUser());
                tLog.setLogTitle(title);
                tLog.setLogContent(content);
                tLog.setUserEmail(userEmail);
                Date date = Date.from(Instant.now());
                Log.d(TAG,"Current Time: " + date);
                tLog.setLogDate(date);

                if (logTitleEdt.getText().toString().length() >= 20) {
                    logTitleEdt.setError("Title cannot exceed 20 characters");
                    return;
                }

                if (logContentEdt.getText().toString().length() >= 225) {
                    logContentEdt.setError("Description cannot exceed 225 characters");
                    return;
                }

                // validating the text fields if empty or not.
                if (TextUtils.isEmpty(title)) {
                    logTitleEdt.setError("Please enter a Title.");
                } else if (TextUtils.isEmpty(content)) {
                    logContentEdt.setError("Please enter a brief Description");
                } else {
                    if (logTitleEdt.length() > 0) {
                        if ( tLog.getLogID() != null) {
                            updateLog(tLog.getLogID(), tLog.getLogTitle(), tLog.getLogContent(), tLog.getLogDate(), tLog.getImgUrl(), tLog.getUserEmail(), tLog.getUser());
                        } else {
                            tLog.setLogID(logID);
                            addDataToFirestore(tLog.getLogTitle(), tLog.getLogContent(), tLog.getImgUrl(), tLog.getUserEmail(), tLog.getLogID(), tLog.getLogDate(), tLog.getUser());
                        }
                    }
                }
            }
        });
    }

    private void updateLog(String ID, String title, String content, Date logDate, String imgUrl, String userEmail, Boolean userType) {

        Map<String, Object> TLog = new HashMap<>();
        TLog.put("userEmail",userEmail);
        TLog.put("logTitle",title);
        TLog.put("logContent",content);
        TLog.put("imgUrl",imgUrl);
        TLog.put("logID",ID);
        TLog.put("logDate",logDate);
        TLog.put("user",userType);

        fStore.collection("travelLog")
                .document(ID)
                .update(TLog)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddTravelLog.this, "Travel Log has been updated!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AddTravelLog.this, TravelLogActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "OnFailure: Error adding Note document", e);
                        Toast.makeText(AddTravelLog.this, "Note could not be updated!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addDataToFirestore(String logTitle, String logContent, String imgUrl, String userEmail, String ID, Date logDate, Boolean userType) {
        // creating a collection reference for our Firebase Firetore database.
        DocumentReference docRef = fStore.collection("travelLog").document(logID);

        tLog.setLogTitle(logTitle);
        tLog.setLogContent(logContent);
        tLog.setImgUrl(imgUrl);
        tLog.setLogID(ID);
        tLog.setUserEmail(userEmail);
        tLog.setLogDate(logDate);
        tLog.setUser(userType);

        // below method is use to add data to Firebase Firestore.
        docRef.set(tLog).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(AddTravelLog.this, "Your travel log has been added to Firebase Firestore", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AddTravelLog.this, TravelLogActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddTravelLog.this, "Fail to add travel log \n" + e, Toast.LENGTH_SHORT).show();
                Log.e(TAG,"onFailure: Fail to save data, " + e.toString());
            }
        });
    }

    // this event will enable the back function to the button on press
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(getApplicationContext(), TravelLogActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void uploadTravelLog(View view) {
        Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(openGalleryIntent,1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK && data.getData() != null) {
                Uri imageUri = data.getData();
                logImgEdt.setVisibility(View.VISIBLE);
                logImgEdt.setImageURI(imageUri);
                uploadImageToFirebase(imageUri);
            }
        } else {
            Toast.makeText(this, "Error, Please try again", Toast.LENGTH_SHORT).show();
            Log.e(TAG,"OnFailure: " + Exception.class.toString());
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        // upload image to firebase storage
        final StorageReference image = Folder.child("travelLog/"+logID+"/image.jpg");
        image.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d("tag", "onSuccess: Uploaded Image URl is " + uri.toString());
                        Picasso.get().load(uri).into(logImgEdt);
                        DocumentReference docRef = fStore.collection("travelLog").document(logID);
                        docRef.update("imgUrl", uri.toString());
                        tLog.setImgUrl(uri.toString());
                    }
                });

                Toast.makeText(AddTravelLog.this, "Image Is Uploaded.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddTravelLog.this, "Upload Failed.", Toast.LENGTH_SHORT).show();
                Log.e(TAG,"OnFailure: "+e.toString());
            }
        });
    }

    private void getUserProfile() {
        DocumentReference docRef = fStore.collection("users").document(userID);
        docRef.addSnapshotListener(AddTravelLog.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                assert documentSnapshot != null;
                if (documentSnapshot.exists()) {
                    user.setFullName(documentSnapshot.getString("name"));
                    user.setEmail(documentSnapshot.getString("email"));
                    user.setPassword(documentSnapshot.getString("password"));
                    user.setPhone(documentSnapshot.getString("phone"));
                    user.setUser(documentSnapshot.getBoolean("isUser"));
                    user.setImgUrl(documentSnapshot.getString("profileImage"));
                    user.setUserId(documentSnapshot.getString("userID"));
                    Log.d(TAG, "getUserProfile: " + user.getUser());
                } else {
                    assert e != null;
                    Log.d(TAG, "onEvent: Documents do not exists, " + e.toString());
                }
            }
        });
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
