package com.ai180183.johortravelrouteplanner;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.ai180183.johortravelrouteplanner.classes.AttractivePlaces;
import com.ai180183.johortravelrouteplanner.classes.Users;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddNewPlace extends AppCompatActivity {
    EditText placeNameEdt, placeDesEdt, placeLat, placeLng, openingHour, closingHour;
    ImageView placeImgEdt;
    AutoCompleteTextView dropdownMenu, districtMenu;
    Button savePlaceBtn, uploadImgBtn;
    TextInputLayout menu;
    ProgressBar progressBar;
    StorageReference Folder;

    FirebaseUser firebaseUser;
    FirebaseFirestore fStore;
    String userID;
    String userEmail;
    String placeID;
    private AttractivePlaces places;
    private final Users user = new Users();
    private static final String TAG = "AddNewPlace";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_place);
        fStore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userEmail = firebaseUser.getEmail();
        userID = firebaseUser.getUid();
        placeID = fStore.collection("attractivePlaces").document().getId();

        placeLat = findViewById(R.id.edit_place_lat);
        placeLng = findViewById(R.id.edit_place_lnt);
        placeImgEdt = findViewById(R.id.edit_place_image);
        placeNameEdt = findViewById(R.id.edit_place_name);
        placeDesEdt = findViewById(R.id.edit_place_description);
        openingHour = findViewById(R.id.edit_workStart);
        closingHour = findViewById(R.id.edit_workEnd);
        savePlaceBtn = findViewById(R.id.saveAttractivePlace);
        uploadImgBtn = findViewById(R.id.uploadPlaceImage);
        dropdownMenu = findViewById(R.id.dropdown_menu);
        districtMenu = findViewById(R.id.district_menu);
        menu = findViewById(R.id.menu);
        progressBar = findViewById(R.id.loadingP);
        Folder = FirebaseStorage.getInstance().getReference();

        places = new AttractivePlaces();

        ArrayList<String> itemList = new ArrayList<>();
        itemList.add("Category Eat");
        itemList.add("Category Play");
        itemList.add("Category Stay");

        ArrayList<String> districtList = new ArrayList<>();
        districtList.add("Batu Pahat");
        districtList.add("Johor Bahru");
        districtList.add("Kluang");
        districtList.add("Kota Tinggi");
        districtList.add("Kulai");
        districtList.add("Mersing");
        districtList.add("Muar");
        districtList.add("Pontian");
        districtList.add("Segamat");
        districtList.add("Tangkak");

        ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.dropdownlist_item, R.id.dropdownItem, itemList);
        dropdownMenu.setAdapter(arrayAdapter);

        ArrayAdapter arrayAdapter1 = new ArrayAdapter(this, R.layout.dropdownlist_item, R.id.dropdownItem, districtList);
        districtMenu.setAdapter(arrayAdapter1);

        // calling the action bar
        ActionBar actionBar = getSupportActionBar();
        // showing the back button in action bar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        progressBar.setVisibility(View.GONE);
        getUserProfile();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            places.setPlaceID(bundle.getString("UpdatePlaceId"));
            places.setPlaceName(bundle.getString("UpdatePlaceName"));
            places.setPlaceDesc(bundle.getString("UpdatePlaceDesc"));
            places.setPlaceImg(bundle.getString("UpdatePlaceImg"));
            places.setPlaceDate((Date) bundle.getSerializable("UpdatePlaceDate"));
            places.setCategory(bundle.getString("UpdateCategory"));
            places.setLat(bundle.getDouble("UpdatePlaceLatitude"));
            places.setLng(bundle.getDouble("UpdatePlaceLongitude"));
            places.setOpeningHour(bundle.getInt("UpdateOpeningHour"));
            places.setClosingHour(bundle.getInt("UpdateClosingHour"));

            placeNameEdt.setText(places.getPlaceName());
            placeDesEdt.setText(places.getPlaceDesc());
            placeLat.setText(String.valueOf(places.getLat()));
            placeLng.setText(String.valueOf(places.getLng()));
            dropdownMenu.setText(places.getCategory());
            districtMenu.setText(places.getDistrict());
            openingHour.setText(places.getOpeningHour());
            closingHour.setText(places.getClosingHour());
            Picasso.get().load(places.getPlaceImg()).into(placeImgEdt);
        }

        savePlaceBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                String title = placeNameEdt.getText().toString();
                String content = placeDesEdt.getText().toString();
                String cate = dropdownMenu.getText().toString();
                String dis = districtMenu.getText().toString();

                Date date = Date.from(Instant.now());
                Log.d(TAG,"Current Time: " + date);
                places.setPlaceDate(date);
                places.setPlaceName(title);
                places.setPlaceDesc(content);
                places.setCategory(cate);
                places.setDistrict(dis);

                if (placeNameEdt.getText().toString().length() >= 20) {
                    placeNameEdt.setError("Title cannot exceed 20 characters");
                    return;
                }
                if (placeDesEdt.getText().toString().length() >= 225) {
                    placeDesEdt.setError("Description cannot exceed 225 characters");
                    return;
                }
                if (dropdownMenu.getText().toString().isEmpty()) {
                    dropdownMenu.setError("Please select a Category");
                }
                if (districtMenu.getText().toString().isEmpty()) {
                    districtMenu.setError("Please select a District");
                }
                // validating the text fields if empty or not.
                if (TextUtils.isEmpty(title)) {
                    placeNameEdt.setError("Please enter a Name.");
                } else if (TextUtils.isEmpty(content)) {
                    placeDesEdt.setError("Please enter a brief Description");
                } else if (TextUtils.isEmpty(content)) {
                    placeLat.setError("Please enter a Latitude");
                } else if (TextUtils.isEmpty(content)) {
                    placeLng.setError("Please enter a Longitude");
                } else if (TextUtils.isEmpty(content)) {
                    openingHour.setError("Please enter Opening Hour");
                } else if (TextUtils.isEmpty(content)) {
                    closingHour.setError("Please enter Closing Hour");
                } else {
                    if (placeNameEdt.length() > 0) {
                        progressBar.setVisibility(View.VISIBLE);
                        if ( places.getPlaceID() != null) {
                            updateLog(places.getPlaceID(), places.getPlaceName(), places.getPlaceDesc(),
                                    places.getPlaceDate(), places.getPlaceImg(), places.getDistrict());
                        } else {
                            places.setPlaceID(placeID);
                            addDataToFirestore(places.getPlaceName(), places.getPlaceDesc(),
                                    places.getDistrict(), places.getPlaceImg(), places.getPlaceID(),
                                    places.getPlaceDate(), places.getCategory());
                        }
                    }
                }
            }
        });
    }

    private void updateLog(String ID, String title, String content, Date logDate, String imgUrl, String dis) {

        double lat = Double.parseDouble(placeLat.getText().toString());
        double lng = Double.parseDouble(placeLng.getText().toString());
        String hash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(lat, lng));

        Map<String, Object> Place = new HashMap<>();
        Place.put("userEmail",userEmail);
        Place.put("placeName",title);
        Place.put("placeDesc",content);
        Place.put("placeImg",imgUrl);
        Place.put("placeID",ID);
        Place.put("placeDate",logDate);
        Place.put("lat",lat);
        Place.put("lng",lng);
        Place.put("hash",hash);
        Place.put("openingHour", Integer.parseInt(openingHour.getText().toString()));
        Place.put("closingHour",Integer.parseInt(closingHour.getText().toString()));
        Place.put("district",dis);

        fStore.collection("attractivePlaces")
                .document(ID)
                .update(Place)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AddNewPlace.this, "Attractive place has been updated!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AddNewPlace.this, ExploreActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "OnFailure: Error updating attractive place", e);
                        Toast.makeText(AddNewPlace.this, "Attractive place could not be updated!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addDataToFirestore(String logTitle, String logContent, String dis, String imgUrl, String ID, Date logDate, String cate) {
        // creating a collection reference
        // for our Firebase Firetore database.
        DocumentReference docRef = fStore.collection("attractivePlaces").document(placeID);

        double lat = Double.parseDouble(placeLat.getText().toString());
        double lng = Double.parseDouble(placeLng.getText().toString());
        String hash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(lat, lng));

        Map<String, Object> place = new HashMap<>();
        place.put("userEmail",userEmail);
        place.put("placeName",logTitle);
        place.put("placeDesc",logContent);
        place.put("placeImg",imgUrl);
        place.put("placeID",ID);
        place.put("placeDate",logDate);
        place.put("category",cate);
        place.put("lat",lat);
        place.put("lng",lng);
        place.put("hash",hash);
        place.put("district",dis);
        place.put("openingHour",Integer.parseInt(openingHour.getText().toString()));
        place.put("closingHour",Integer.parseInt(closingHour.getText().toString()));

        // below method is use to add data to Firebase Firestore.
        docRef.set(place).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddNewPlace.this, "Attractive Place has been added to Firebase Firestore", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AddNewPlace.this, ExploreActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddNewPlace.this, "Failed to add a new Attractive Place!", Toast.LENGTH_SHORT).show();
                Log.e(TAG,"onFailure: Failed to add a new Attractive Place, " + e.toString());
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

    public void uploadPlaceImage(View view) {
        Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(openGalleryIntent,1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK && data.getData() != null) {
                Uri imageUri = data.getData();
                placeImgEdt.setVisibility(View.VISIBLE);
                placeImgEdt.setImageURI(imageUri);
                uploadImageToFirebase(imageUri);
            }
        } else {
            Toast.makeText(this, "Error, Please try again", Toast.LENGTH_SHORT).show();
            Log.e(TAG,"OnFailure: " + Exception.class.toString());
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        // upload image to firebase storage
        final StorageReference image = Folder.child("attractivePlaces/"+placeID+"/image.jpg");
        image.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d("tag", "onSuccess: Uploaded Image URl is " + uri.toString());
                        Picasso.get().load(uri).into(placeImgEdt);
                        DocumentReference docRef = fStore.collection("attractivePlaces").document(placeID);
                        docRef.update("imgUrl", uri.toString());
                        places.setPlaceImg(uri.toString());
                    }
                });

                Toast.makeText(AddNewPlace.this, "Image Is Uploaded.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddNewPlace.this, "Upload Failed.", Toast.LENGTH_SHORT).show();
                Log.e(TAG,"OnFailure: "+e.toString());
            }
        });
    }

    private void getUserProfile() {
        DocumentReference docRef = fStore.collection("users").document(userID);
        docRef.addSnapshotListener(AddNewPlace.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (documentSnapshot!=null && documentSnapshot.exists()) {
                    user.setFullName(documentSnapshot.getString("name"));
                    user.setEmail(documentSnapshot.getString("email"));
                    user.setPassword(documentSnapshot.getString("password"));
                    user.setPhone(documentSnapshot.getString("phone"));
                    user.setUser(documentSnapshot.getBoolean("isUser"));
                    user.setImgUrl(documentSnapshot.getString("profileImage"));
                    user.setUserId(documentSnapshot.getString("userID"));
                    Log.d(TAG, "getUserProfile: " + user.getUser());
                } else {
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
