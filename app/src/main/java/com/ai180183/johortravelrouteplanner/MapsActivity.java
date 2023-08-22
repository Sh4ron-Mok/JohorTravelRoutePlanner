package com.ai180183.johortravelrouteplanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ai180183.johortravelrouteplanner.interfaces.ConstantInterface;
import com.ai180183.johortravelrouteplanner.util.AppUtility;
import com.ai180183.johortravelrouteplanner.util.RunTimePermission;
import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.maps.model.PlaceDetails;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class MapsActivity extends AppCompatActivity implements ConstantInterface, OnMapReadyCallback, DirectionCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, SensorEventListener {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;
    private static final String CURRENT_LOCATION_TITLE = "Current Location";
    private static final String NOT_CURRENT_LOCATION_TITLE = "Unable To Find Current Location";
    private static final String PANEL_STATE_EDIT_OPTIMIZE = "panelStateEditOptimize";
    private static final String PANEL_STATE_DIRECTIONS = "panelStateDirections";
    private static final String CAMERA_LATITUDE = "cameraLatitude";
    private static final String CAMERA_LONGITUDE = "cameraLongitude";
    private static final String CAMERA_ZOOM = "cameraZoom";
    private static final String RETURN_HOME_CLICKED = "returnHomeClicked";
    public static double DEFAULT_LATITUDE = 0;
    public static double DEFAULT_LONGITUDE = 0;
    public static String DEFAULT_LATITUDE_STRING = "0";
    public static String DEFAULT_LONGITUDE_STRING = "0";
    private static final float DEFAULT_ZOOM = 15;
    private static final int DELAY_TIME = 300;
    private static final double EPS = 0.00001;
    private static final int MAP_PADDING = 300;
    public static final String LAT = "lat";
    public static final String LNG = "lng";
    public static final String NUM_RESULTS_RETURNED = "5";
    public static final String SEARCH_HISTORY_FILENAME = "searchHistoryFilename";
    public static final String PLACE_ID_FILENAME = "placeIdFilename";
    public static final int MAX_CHAR = 100000;
    public SupportMapFragment mapFragment = new SupportMapFragment();

    boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    public Location mCurrentLocation;
    private LocationCallback locationCallback;
    public ImageButton mReturnHomeButton;
    public SearchView mSearchView;
    public ProgressBar mProgressBarMap;
    public BottomNavigationView mBottomNavigationView;
    public int mAddPosition = -1;
    public int mEditPosition = -1;
    public Button mAddLocationSubmitBtn;
    public SearchHistoryAsyncQueryHandler mSearchHistoryAsyncQueryHandler;

    SensorManager mSensorManager;
    private Sensor mSensorAccelerometer;
    private Sensor mSensorMagneticField;

    public GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private RunTimePermission runTimePermission;
    private final String TAG = "MapsActivity";
    private float[] mValuesAccelerometer;
    private float[] mValuesMagneticField;
    private float[] mMatrixR;
    private float[] mMatrixI;
    private float[] mMatrixValues;
    private float mPrevOrientation = 0;
    public boolean mReturnHomeClicked = true;
    private LatLng mCameraPosition = null;
    private float mCameraZoom = -1;

    public FragmentAdd mFragmentAdd;
    public BottomSheetFragmentEditOptimize mBottomSheetFragmentEditOptimize;
    public BottomSheetFragmentDirections mBottomSheetFragmentDirections;
    public FrameLayout mAddLocationContainer;
    public SlidingUpPanelLayout mSlidingUpPanelLayout;
    public int mBottomNavigationViewState;
    public Marker mCurrentLocationMarker;
    public boolean mOptimizeClicked = false;
    public String mPlaceIdsAdd = "";
    public int mWhoseMarkers = R.id.directions;
    ResponseReceiver mReceiver;
    public OptimizeRouteTask mOptimizeRouteTask;

    public ArrayList<String> mComposeRoute = new ArrayList<>();
    public ArrayList<JSONParser.PlaceDetails> mComposePlaceDetails = new ArrayList<>();
    public ArrayList<PlaceDetails> mGetPlaceDetails = new ArrayList<>();
    public ArrayList<Boolean> mComposeFlagged = new ArrayList<>();
    public ArrayList<Integer> mComposeMarkerColors = new ArrayList<>();
    public Queue<Integer> mComposeUnusedColors = new LinkedList<>();
    public ArrayList<Marker> mComposeMarkers = new ArrayList<>();

    public ArrayList<String> mDirectionsRoute = new ArrayList<>();
    public ArrayList<JSONParser.PlaceDetails> mDirectionsPlaceDetails = new ArrayList<>();
    public ArrayList<Boolean> mDirectionsFlagged = new ArrayList<>();
    public ArrayList<Integer> mDirectionsMarkerColors = new ArrayList<>();
    public Queue<Integer> mDirectionsUnusedColors = new LinkedList<>();
    public ArrayList<Marker> mDirectionsMarkers = new ArrayList<>();
    public ArrayList<JSONParser.PlaceDetails> mDirectionsOptimizedRoute = new ArrayList<>();
    public ArrayList<Integer> mDirectionsOrder = new ArrayList<>();
    public ArrayList<ArrayList<ArrayList<HashMap<String, String>>>> mDirections = new ArrayList<>();

    public static StringBuilder mSearchHistory = new StringBuilder();
    public static String[] mSearchHistoryRegex = new String[0];
    public static StringBuilder mPlaceId = new StringBuilder();
    public static String[] mPlaceIdRegex = new String[0];
    public static SuffixTree.Node mRoot;
    public static int[] mCharToPos = new int[MAX_CHAR + 5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        File directory = getApplicationContext().getFilesDir();
        File searchHistoryFile = new File(directory, SEARCH_HISTORY_FILENAME);
        if (searchHistoryFile.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(searchHistoryFile));
                String line;

                while ((line = br.readLine()) != null) {
                    mSearchHistory.append(line);
                    mSearchHistory.append('\n');
                }
                br.close();

                mSearchHistoryRegex = mSearchHistory.toString().split("\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String searchHistory = mSearchHistory.toString();
        for (int i = 0, cnt = 0; i < searchHistory.length(); i++) {
            mCharToPos[i] = cnt;
            if (searchHistory.charAt(i) == '\n') {
                cnt++;
            }
        }

        File placeIdFile = new File(directory, PLACE_ID_FILENAME);
        if (placeIdFile.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(placeIdFile));
                String line;

                while ((line = br.readLine()) != null) {
                    mPlaceId.append(line);
                    mPlaceId.append('\n');
                }
                br.close();

                mPlaceIdRegex = mPlaceId.toString().split("\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        mRoot = SuffixTreeHelper.constructSuffixTree(mSearchHistory.toString());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        initControls();
        getLocationPermission();

        mSearchView = findViewById(R.id.searchView);
        mReturnHomeButton = findViewById(R.id.returnHome);
        mProgressBarMap = findViewById(R.id.progressBarMap);
        mBottomNavigationView = findViewById(R.id.bottomNavigationView);
        mSlidingUpPanelLayout = findViewById(R.id.sliding_layout);

        // calling the action bar
        ActionBar actionBar = getSupportActionBar();
        // showing the back button in action bar
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
        Places.initialize(MapsActivity.this, "AIzaSyAW93vjA1Ot2iZadSISqr9PnnKd4564bLY");
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        mAddLocationContainer = findViewById(R.id.add_location_container);
        mFragmentAdd = new FragmentAdd();
        getSupportFragmentManager().beginTransaction().add(
                R.id.add_location_container, mFragmentAdd).commit();

        mBottomSheetFragmentDirections = new BottomSheetFragmentDirections();
        mBottomSheetFragmentEditOptimize = new BottomSheetFragmentEditOptimize();
        getSupportFragmentManager().beginTransaction().add(
                R.id.bottom_sheet_fragment, mBottomSheetFragmentDirections).commit();

        BottomNavigationMenuView menuView = (BottomNavigationMenuView) mBottomNavigationView.getChildAt(0);

        for (int i = 0; i < menuView.getChildCount(); i++) {
            BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
            View activeLabel = item.findViewById(R.id.largeLabel);
            if (activeLabel instanceof TextView) {
                activeLabel.setPadding(0, 0, 0, 0);
            }
        }

        mBottomNavigationViewState = R.id.explore;

        mBottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @SuppressLint("NonConstantResourceId")
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.explore:
                                mAddLocationContainer.setVisibility(View.GONE);
                                hideKeyboard(MapsActivity.this);
                                mapFragment.getView().setAlpha(1f);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                                    }
                                }, DELAY_TIME);
                                mBottomNavigationViewState = R.id.explore;
                                return true;

                            case R.id.add_location:
                                mAddLocationContainer.setVisibility(View.VISIBLE);
                                mapFragment.getView().setAlpha(0.4f);
                                mSearchView.requestFocus();
                                mAddPosition = mComposeRoute.size();
                                if (mAddLocationSubmitBtn != null) {
                                    mAddLocationSubmitBtn.setText(getString(R.string.add_location));
                                }
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                                    }
                                }, DELAY_TIME);
                                return true;

                            case R.id.edit_locations:
                                mAddLocationContainer.setVisibility(View.GONE);
                                hideKeyboard(MapsActivity.this);
                                mapFragment.getView().setAlpha(1f);
                                showMarkers(R.id.edit_locations);
                                getSupportFragmentManager().beginTransaction().replace(
                                        R.id.bottom_sheet_fragment,
                                        mBottomSheetFragmentEditOptimize).commit();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                                    }
                                }, DELAY_TIME);

                                mWhoseMarkers = R.id.edit_locations;
                                mBottomNavigationViewState = R.id.edit_locations;
                                return true;

                            case R.id.directions:
                                mAddLocationContainer.setVisibility(View.GONE);
                                hideKeyboard(MapsActivity.this);
                                mapFragment.getView().setAlpha(1f);
                                if (!mOptimizeClicked) showMarkers(R.id.directions);
                                getSupportFragmentManager().beginTransaction().replace(
                                        R.id.bottom_sheet_fragment,
                                        mBottomSheetFragmentDirections).commit();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                                    }
                                }, DELAY_TIME);

                                mWhoseMarkers = R.id.directions;
                                mBottomNavigationViewState = R.id.directions;
                                return true;
                        }

                        return false;
                    }
                }
        );

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;
        }
        String provider = locationManager.getBestProvider(criteria, true);
        mCurrentLocation = locationManager.getLastKnownLocation(provider);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    mCurrentLocation = location;
                    showCurrentLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                            true, false);
                    Log.d(TAG,"Current location is " +mCurrentLocation.getLatitude()+", "+mCurrentLocation.getLongitude());
                }
            }

            ;
        };

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mValuesAccelerometer = new float[3];
        mValuesMagneticField = new float[3];

        mMatrixR = new float[9];
        mMatrixI = new float[9];
        mMatrixValues = new float[3];

        mProgressBarMap.setVisibility(View.GONE);

        mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        final int[] MARKER_COLORS = getResources().getIntArray(R.array.markerColors);
        for (int markerColor : MARKER_COLORS) {
            mComposeUnusedColors.add(markerColor);
        }

        mOptimizeRouteTask = new OptimizeRouteTask(this);

        mSearchHistoryAsyncQueryHandler = new SearchHistoryAsyncQueryHandler(getContentResolver());
    }

    private void initControls() {
        AppUtility appUtility = new AppUtility(this);
        runTimePermission = new RunTimePermission(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if (appUtility.checkPlayServices()) {
            googleApiClient = new GoogleApiClient
                    .Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            googleApiClient.connect();
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setInfoWindowAdapter(new GoogleMapsInfoWindowAdapter(this));

        showInitialLocation();

        mReturnHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng locationToDisplay = new LatLng(mCurrentLocation.getLatitude(),
                        mCurrentLocation.getLongitude());
                float zoom = googleMap.getCameraPosition().zoom;
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationToDisplay, zoom));
                mReturnHomeButton.setImageResource(R.drawable.ic_baseline_near_me_24);
                mReturnHomeClicked = true;
            }
        });

        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                LatLng cameraPosition = googleMap.getCameraPosition().target;
                if (Math.abs(cameraPosition.latitude - mCurrentLocation.getLatitude()) > EPS
                        || Math.abs(cameraPosition.longitude - mCurrentLocation.getLongitude()) > EPS) {
                    mReturnHomeButton.setImageResource(R.drawable.ic_baseline_near_me_24);
                    mReturnHomeClicked = false;
                }
            }
        });
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            } else {
                Toast.makeText(this,
                        "Location Permission Denied. Going to Default Location.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showInitialLocation() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            DEFAULT_LATITUDE = bundle.getDouble("DEFAULT_LATITUDE");
            DEFAULT_LONGITUDE = bundle.getDouble("DEFAULT_LONGITUDE");
        }
        if (mLocationPermissionGranted) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Task<Location> task = mFusedLocationProviderClient.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
//                    if (location != null) {
//                        mCurrentLocation = location;
//                        showCurrentLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
//                                true, true);
//                    } else {
                        showCurrentLocation(DEFAULT_LATITUDE, DEFAULT_LONGITUDE, false, true);
//                    }
                }
            });
        } else {
            showCurrentLocation(DEFAULT_LATITUDE, DEFAULT_LONGITUDE, false, true);
        }
    }

    private void showCurrentLocation(double latitude, double longitude, boolean isCurrentLocation,
                                     boolean initialLocation) {
        LatLng locationToDisplay = new LatLng(latitude, longitude);
        if (mCurrentLocationMarker == null) {
            mCurrentLocationMarker = addCurrentLocationMarker(locationToDisplay, isCurrentLocation);
        } else {
            mCurrentLocationMarker.setPosition(locationToDisplay);
            mCurrentLocationMarker.setTitle(
                    isCurrentLocation ? CURRENT_LOCATION_TITLE : NOT_CURRENT_LOCATION_TITLE);
            mCurrentLocationMarker.setIcon(BitmapDescriptorFactory.fromBitmap(createMarker()));
            mCurrentLocationMarker.setFlat(true);
            mCurrentLocationMarker.setAnchor(0.5f, 0.5f);
            mCurrentLocationMarker.setRotation(mPrevOrientation);
        }
        if (initialLocation) {
            float zoom = DEFAULT_ZOOM;
            if (mCameraPosition != null) locationToDisplay = mCameraPosition;
            if (mCameraZoom != -1) zoom = mCameraZoom;
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationToDisplay, zoom));
        } else if (mReturnHomeClicked) {
            float zoom = googleMap.getCameraPosition().zoom;
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationToDisplay, zoom));
        }
    }

    private void stopLocationUpdates() {
        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void startLocationUpdates() {
        if (mLocationPermissionGranted) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                mFusedLocationProviderClient.requestLocationUpdates(createLocationRequest(),
                        locationCallback, null);
                return;
            }
        }
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 51) {
            if (resultCode == RESULT_OK) {
                getDeviceLocation();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            mCurrentLocation = task.getResult();
                            if (mCurrentLocation != null) {
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), DEFAULT_ZOOM));
                            } else {
                                final LocationRequest locationRequest = LocationRequest.create();
                                locationRequest.setInterval(10000);
                                locationRequest.setFastestInterval(5000);
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                locationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if (locationResult == null) {
                                            return;
                                        }
                                        mCurrentLocation = locationResult.getLastLocation();
                                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), DEFAULT_ZOOM));
                                        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                    }
                                };
                                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

                            }
                        } else {
                            Toast.makeText(MapsActivity.this, "unable to get last location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        if (direction.isOK()) {
            ArrayList<LatLng> directionPositionList = direction.getRouteList()
                    .get(0).getLegList().get(0).getDirectionPoint();
            googleMap.addPolyline(DirectionConverter.createPolyline(this,
                    directionPositionList, 6, ContextCompat.getColor(this,
                            R.color.black)));
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {

    }

    // checking Runtime permission
    private void getPermissions(String[] strings) {

        runTimePermission.requestPermission(strings, new RunTimePermission.RunTimePermissionListener() {

            @Override
            public void permissionGranted() {
                locationChecker(googleApiClient, MapsActivity.this);
            }

            @Override
            public void permissionDenied() {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    // Checking whether location service is enable or not.
    public void locationChecker(GoogleApiClient mGoogleApiClient, final Activity activity) {

        // Creating location request object
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setSmallestDisplacement(DISPLACEMENT);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        getLocation();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(activity, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            Log.e(TAG, "onFailure: "+e.toString());
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, (com.google.android.gms.location.LocationListener) this);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (googleMap == null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                for (int i=0; i<3; i++) {
                    mValuesAccelerometer[i] = event.values[i];
                }
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                for (int i=0; i<3; i++) {
                    mValuesMagneticField[i] = event.values[i];
                }
                break;

        }

        boolean success = SensorManager.getRotationMatrix(mMatrixR, mMatrixI,
                mValuesAccelerometer, mValuesMagneticField);

        if (success) {
            SensorManager.getOrientation(mMatrixR, mMatrixValues);

            mPrevOrientation = (float) Math.toDegrees(mMatrixValues[0]) + 180;

            if (mCurrentLocationMarker != null) {
                mCurrentLocationMarker.setRotation(mPrevOrientation);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(PANEL_STATE_EDIT_OPTIMIZE, mSlidingUpPanelLayout.getPanelState().ordinal());
        outState.putInt(PANEL_STATE_DIRECTIONS, mSlidingUpPanelLayout.getPanelState().ordinal());
        outState.putDouble(CAMERA_LATITUDE,
                (googleMap == null ? DEFAULT_LATITUDE : googleMap.getCameraPosition().target.latitude));
        outState.putDouble(CAMERA_LONGITUDE,
                (googleMap == null ? DEFAULT_LONGITUDE : googleMap.getCameraPosition().target.longitude));
        outState.putFloat(CAMERA_ZOOM,
                (googleMap == null ? DEFAULT_ZOOM : googleMap.getCameraPosition().zoom));
        outState.putBoolean(RETURN_HOME_CLICKED, mReturnHomeClicked);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mSlidingUpPanelLayout.setPanelState(
                SlidingUpPanelLayout.PanelState.values()[savedInstanceState.getInt(PANEL_STATE_EDIT_OPTIMIZE)]);
        mSlidingUpPanelLayout.setPanelState(
                SlidingUpPanelLayout.PanelState.values()[savedInstanceState.getInt(PANEL_STATE_DIRECTIONS)]);

        double cameraLatitude = savedInstanceState.getDouble(CAMERA_LATITUDE);
        double cameraLongitude = savedInstanceState.getDouble(CAMERA_LONGITUDE);
        mCameraPosition = new LatLng(cameraLatitude, cameraLongitude);
        mCameraZoom = savedInstanceState.getFloat(CAMERA_ZOOM);

        mReturnHomeClicked = savedInstanceState.getBoolean(RETURN_HOME_CLICKED);
        if (mReturnHomeClicked) mReturnHomeButton.setImageResource(R.drawable.ic_baseline_near_me_24);
        else mReturnHomeButton.setImageResource(R.drawable.ic_outline_near_me_24);
    }

    public class ResponseReceiver extends BroadcastReceiver {
        public static final String RESPONSE_ACTION =
                "com.ai180183.johortravelrouteplanner.intent.action.MESSAGE_PROCESSED";

        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<String> placeDescriptions = intent.getStringArrayListExtra(
                    GetPlaceAutocompleteIntentService.DESCRIPTIONS);
            ArrayList<String> placeIds = intent.getStringArrayListExtra(
                    GetPlaceAutocompleteIntentService.PLACE_IDS);

            mFragmentAdd.mSearchResultsAdapter.setPlaceDescription(placeDescriptions,
                    mFragmentAdd.mSearchResultsExpanded);
            mFragmentAdd.mSearchResultsAdapter.setPlaceIds(placeIds);
        }
    }

    public void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
    }

    public static void showKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,0);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void showMarkers(int id) {
        showMarkers(id, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void showMarkers(int id, boolean optimizeClicked) {
        if (mWhoseMarkers == id && !optimizeClicked) return;
        googleMap.clear();
        mCurrentLocationMarker = addCurrentLocationMarker(new LatLng(mCurrentLocation.getLatitude(),
                mCurrentLocation.getLongitude()), true);
        switch (id) {
            case R.id.edit_locations:
                mComposeMarkers.clear();

                for (int i=0; i<mComposeRoute.size(); i++) {
                    JSONParser.PlaceDetails placeDetails = mComposePlaceDetails.get(i);
                    if (placeDetails != null && placeDetails.getLatLng().first != null
                            && placeDetails.getLatLng().second != null) {
                        mComposeMarkers.add(googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(
                                        Double.parseDouble(placeDetails.getLatLng().first),
                                        Double.parseDouble(placeDetails.getLatLng().second)))
                                .title((placeDetails.getName() != null
                                        && placeDetails.getAddress() != null
                                        ? getString(R.string.name_address, placeDetails.getName(),
                                        placeDetails.getAddress())
                                        : (placeDetails.getName() == null
                                        ? getString(R.string.name_address_not_found)
                                        : getString(R.string.name,
                                        placeDetails.getName()))))
                                .icon(BitmapDescriptorFactory.fromBitmap(createMarker(
                                        mComposeMarkerColors.get(i), i)))));
                    } else {
                        mComposeMarkers.add(null);
                    }
                }
                break;

            case R.id.directions:
                mDirectionsMarkers.clear();

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                if (mDirectionsOptimizedRoute.size() == 0) {
                    builder.include(mCurrentLocationMarker.getPosition());
                }
                for (int i=0; i<mDirectionsOptimizedRoute.size(); i++) {
                    JSONParser.PlaceDetails placeDetails = mDirectionsOptimizedRoute.get(i);
                    LatLng location = new LatLng(
                            Double.parseDouble(placeDetails.getLatLng().first),
                            Double.parseDouble(placeDetails.getLatLng().second));
                    mDirectionsMarkers.add(googleMap.addMarker(new MarkerOptions()
                            .position(location)
                            .title(getTitle(placeDetails))
                            .icon(BitmapDescriptorFactory.fromBitmap(createMarker(
                                    mDirectionsMarkerColors.get(mDirectionsOrder.get(i)), i)))));
                    builder.include(location);
                }

                if (optimizeClicked) {
                    LatLngBounds bounds = builder.build();
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, MAP_PADDING);
                    googleMap.animateCamera(cameraUpdate, new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            drawRoutes();
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
                } else {
                    drawRoutes();
                }

                break;

            default:
                break;
        }
    }

    private String getTitle(JSONParser.PlaceDetails placeDetails) {
        if (placeDetails.getName() == null && placeDetails.getAddress() == null) {
            return getString(R.string.name_address_not_found);
        }
        return getString(R.string.name_address,
                (placeDetails.getName() == null ? "" : placeDetails.getName()),
                (placeDetails.getAddress() == null ? "" : placeDetails.getAddress()));
    }

    public Marker addCurrentLocationMarker(LatLng locationToDisplay, boolean isCurrentLocation) {
        return googleMap.addMarker(new MarkerOptions()
                .position(locationToDisplay)
                .title(isCurrentLocation ? CURRENT_LOCATION_TITLE : NOT_CURRENT_LOCATION_TITLE)
                .icon(BitmapDescriptorFactory.fromBitmap(createMarker()))
                .flat(true)
                .anchor(0.5f, 0.5f)
                .rotation(mPrevOrientation));
    }

    private Bitmap createMarker() {
        Bitmap bmp = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        Paint outerCircle = new Paint();
        outerCircle.setColor(ContextCompat.getColor(this, R.color.white));
        outerCircle.setShadowLayer(10, 0, 0,
                ContextCompat.getColor(this, R.color.marker_shadow));
        canvas.drawCircle(40, 40, 30, outerCircle);

        Paint triangle = new Paint();
        triangle.setColor(ContextCompat.getColor(this, R.color.primary));
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(40, 80);
        path.lineTo(22.68f, 50);
        path.lineTo(57.32f, 50);
        path.lineTo(40, 80);
        path.close();

        canvas.drawPath(path, triangle);

        Paint midCircle = new Paint();
        midCircle.setColor(ContextCompat.getColor(this, R.color.white));
        canvas.drawCircle(40, 40, 23, midCircle);

        Paint innerCircle = new Paint();
        innerCircle.setColor(ContextCompat.getColor(this, R.color.primary));
        canvas.drawCircle(40, 40, 20, innerCircle);

        return bmp;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public Bitmap createMarker(int color, int position) {
        Bitmap bitmap = Bitmap.createBitmap(60, (position != -1 ? 145 : 110),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint outerCircle = new Paint();
        outerCircle.setColor(color);
        canvas.drawCircle(30, 30, 30, outerCircle);

        Paint triangle = new Paint();
        triangle.setColor(color);
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(57.1052370872f, 42.8571428571f);
        path.lineTo(2.8947629128f, 42.8571428571f);
        path.arcTo(new RectF(-195.14509716f, 10.5258572168f, 28.6941206838f, 234.365075061f),
                334.623066467f, 18.99243571f);
        path.arcTo(new RectF(31.3058793162f, 10.5258572168f, 255.14509716f, 234.365075061f),
                186.384497823f, 18.99243571f);
        path.close();

        canvas.drawPath(path, triangle);

        Paint innerCircle = new Paint();
        innerCircle.setColor(getColor(R.color.white));
        canvas.drawCircle(30, 30, 10, innerCircle);

        if (position != -1) {
            String pos = Integer.toString(position+1);

            Paint number = new Paint();
            number.setColor(getColor(R.color.primary));
            number.setTextSize(25);
            Rect bounds = new Rect();
            number.getTextBounds(pos, 0, pos.length(), bounds);

            int rx = 30;
            int ry = 120;
            int r = 25;

            Paint numberOuterCircle = new Paint();
            numberOuterCircle.setColor(getColor(R.color.black));
            canvas.drawCircle(
                    rx, ry, r, numberOuterCircle);

            Paint numberInnerCircle = new Paint();
            numberInnerCircle.setColor(getColor(R.color.white));
            canvas.drawCircle(
                    rx, ry, r-3, numberInnerCircle);

            canvas.drawText(pos, rx-bounds.width()/2f, ry+bounds.height()/2f, number);
        }

        return bitmap;
    }

    private void drawRoutes() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            DEFAULT_LATITUDE_STRING = bundle.getString("DEFAULT_LATITUDE_STRING");
            DEFAULT_LONGITUDE_STRING = bundle.getString("DEFAULT_LONGITUDE_STRING");
        }

        ArrayList<Integer> unableToDrawRoutes = new ArrayList<>();

        for (int t=0; t<mDirections.size(); t++) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i=0; i<mDirections.get(t).size(); i++) {
                if (mDirections.get(t).get(i) == null) {
                    unableToDrawRoutes.add(t);
                    continue;
                }

                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                ArrayList<HashMap<String, String>> path = mDirections.get(t).get(i);

                // Fetching all the points in i-th route
                for (int j=0; j<path.size(); j++){
                    HashMap<String, String> point = path.get(j);

                    double lat, lng;
                    if (point.containsKey(LAT)) {
                        lat = Double.parseDouble(point.get(LAT));
                    } else {
                        lat = Double.parseDouble(MapsActivity.DEFAULT_LATITUDE_STRING);
                    }

                    if (point.containsKey(LNG)) {
                        lng = Double.parseDouble(point.get(LNG));
                    } else {
                        lng = Double.parseDouble(MapsActivity.DEFAULT_LONGITUDE_STRING);
                    }

                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(mDirectionsMarkerColors.get(
                        mDirectionsOrder.get(t)));
            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                googleMap.addPolyline(lineOptions);
            } else {
                unableToDrawRoutes.add(t);
            }
        }

        StringBuilder stringBuilder = new StringBuilder(getString(
                R.string.unable_to_draw_route));
        for (int i=0; i<unableToDrawRoutes.size(); i++) {
            stringBuilder.append((i > 0 ? ", " : "") + unableToDrawRoutes.get(i));
        }

        if (unableToDrawRoutes.size() > 0) {
            Toast.makeText(MapsActivity.this, stringBuilder.toString(),
                    Toast.LENGTH_LONG).show();
        }
    }

    // this event will enable the back function to the button on press
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(getApplicationContext(), PlanActivity.class));
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
        IntentFilter filter = new IntentFilter(ResponseReceiver.RESPONSE_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        mReceiver = new ResponseReceiver();
        registerReceiver(mReceiver, filter);
        mSensorManager.registerListener(this, mSensorAccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorMagneticField,
                SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
        Log.d(TAG,"on Resume");
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mReceiver);
        mSensorManager.unregisterListener(this, mSensorAccelerometer);
        mSensorManager.unregisterListener(this, mSensorMagneticField);
        super.onPause();
        Log.d(TAG,"on Pause");
        stopLocationUpdates();
    }

    @Override
    public void onStop() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
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