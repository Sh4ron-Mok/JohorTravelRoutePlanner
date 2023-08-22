package com.ai180183.johortravelrouteplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PlanActivity extends AppCompatActivity {
    private static final String TAG = "PlanActivity";
    com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigation;
    Button bp, jb, kluang, kt, kulai, muar, mersing, pontian, segamat, tangkak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);

        bp = findViewById(R.id.batupahat);
        jb = findViewById(R.id.johorbahru);
        kluang = findViewById(R.id.kluang);
        kt = findViewById(R.id.kotatinggi);
        kulai = findViewById(R.id.kulai);
        muar = findViewById(R.id.muar);
        mersing = findViewById(R.id.mersing);
        pontian = findViewById(R.id.pontian);
        segamat = findViewById(R.id.segamat);
        tangkak = findViewById(R.id.tangkak);

        BottomNavigationView bottomNavigationView;
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_plan);

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_explore:
                        Intent intent1 = new Intent(PlanActivity.this, ExploreActivity.class);
                        startActivity(intent1);
                        finish();
                        return true;
                    case R.id.navigation_plan:
                        Intent intent2 = new Intent(PlanActivity.this, PlanActivity.class);
                        startActivity(intent2);
                        finish();
                        return true;
                    case R.id.navigation_log:
                        Intent intent3 = new Intent(PlanActivity.this, TravelLogActivity.class);
                        startActivity(intent3);
                        finish();
                        return true;
                    case R.id.navigation_account:
                        Intent intent4 = new Intent(PlanActivity.this, AccountActivity.class);
                        startActivity(intent4);
                        finish();
                        return true;
                }
                return false;
            }
        });

        bp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentBP = new Intent(PlanActivity.this, MapsActivity.class);
                intentBP.putExtra("DEFAULT_LATITUDE",1.8494);
                intentBP.putExtra("DEFAULT_LONGITUDE",102.9288);
                intentBP.putExtra("DEFAULT_LATITUDE_STRING","1.8494");
                intentBP.putExtra("DEFAULT_LONGITUDE_STRING","102.9288");
                startActivity(intentBP);
                finish();
            }
        });

        jb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentJB = new Intent(PlanActivity.this, MapsActivity.class);
                intentJB.putExtra("DEFAULT_LATITUDE",1.4927);
                intentJB.putExtra("DEFAULT_LONGITUDE",103.7414);
                intentJB.putExtra("DEFAULT_LATITUDE_STRING","1.4927");
                intentJB.putExtra("DEFAULT_LONGITUDE_STRING","103.7414");
                startActivity(intentJB);
                finish();
            }
        });

        kluang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentK = new Intent(PlanActivity.this, MapsActivity.class);
                intentK.putExtra("DEFAULT_LATITUDE",2.0301);
                intentK.putExtra("DEFAULT_LONGITUDE",103.3185);
                intentK.putExtra("DEFAULT_LATITUDE_STRING","2.0301");
                intentK.putExtra("DEFAULT_LONGITUDE_STRING","103.3185");
                startActivity(intentK);
                finish();
            }
        });

        kt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentKT = new Intent(PlanActivity.this, MapsActivity.class);
                intentKT.putExtra("DEFAULT_LATITUDE",1.7294);
                intentKT.putExtra("DEFAULT_LONGITUDE",103.8992);
                intentKT.putExtra("DEFAULT_LATITUDE_STRING","1.7294");
                intentKT.putExtra("DEFAULT_LONGITUDE_STRING","103.8992");
                startActivity(intentKT);
                finish();
            }
        });

        kulai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentKi = new Intent(PlanActivity.this, MapsActivity.class);
                intentKi.putExtra("DEFAULT_LATITUDE",1.6630);
                intentKi.putExtra("DEFAULT_LONGITUDE",103.5999);
                intentKi.putExtra("DEFAULT_LATITUDE_STRING","1.6630");
                intentKi.putExtra("DEFAULT_LONGITUDE_STRING","103.5999");
                startActivity(intentKi);
                finish();
            }
        });

        muar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentM = new Intent(PlanActivity.this, MapsActivity.class);
                intentM.putExtra("DEFAULT_LATITUDE",2.0631);
                intentM.putExtra("DEFAULT_LONGITUDE",102.5849);
                intentM.putExtra("DEFAULT_LATITUDE_STRING","2.0631");
                intentM.putExtra("DEFAULT_LONGITUDE_STRING","102.5849");
                startActivity(intentM);
                finish();
            }
        });

        mersing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentMS = new Intent(PlanActivity.this, MapsActivity.class);
                intentMS.putExtra("DEFAULT_LATITUDE",2.4309);
                intentMS.putExtra("DEFAULT_LONGITUDE",103.8361);
                intentMS.putExtra("DEFAULT_LATITUDE_STRING","2.4309");
                intentMS.putExtra("DEFAULT_LONGITUDE_STRING","103.8361");
                startActivity(intentMS);
                finish();
            }
        });

        pontian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentP = new Intent(PlanActivity.this, MapsActivity.class);
                intentP.putExtra("DEFAULT_LATITUDE",1.4869);
                intentP.putExtra("DEFAULT_LONGITUDE",103.3890);
                intentP.putExtra("DEFAULT_LATITUDE_STRING","1.4869");
                intentP.putExtra("DEFAULT_LONGITUDE_STRING","103.3890");
                startActivity(intentP);
                finish();
            }
        });

        segamat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentS = new Intent(PlanActivity.this, MapsActivity.class);
                intentS.putExtra("DEFAULT_LATITUDE",2.5035);
                intentS.putExtra("DEFAULT_LONGITUDE",102.8208);
                intentS.putExtra("DEFAULT_LATITUDE_STRING","2.5035");
                intentS.putExtra("DEFAULT_LONGITUDE_STRING","102.8208");
                startActivity(intentS);
                finish();
            }
        });

        tangkak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentT = new Intent(PlanActivity.this, MapsActivity.class);
                intentT.putExtra("DEFAULT_LATITUDE",2.2711);
                intentT.putExtra("DEFAULT_LONGITUDE",102.5420);
                intentT.putExtra("DEFAULT_LATITUDE_STRING","2.2711");
                intentT.putExtra("DEFAULT_LONGITUDE_STRING","102.5420");
                startActivity(intentT);
                finish();
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