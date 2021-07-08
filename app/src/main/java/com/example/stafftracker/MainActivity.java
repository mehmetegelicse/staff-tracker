package com.example.stafftracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.Activity;

import android.app.Person;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.os.Bundle;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stafftracker.utils.BackgroundService;
import com.example.stafftracker.utils.CheckPermission;
import com.example.stafftracker.utils.LocationService;
import com.example.stafftracker.utils.PermissionUtils;
import com.example.stafftracker.view.BottomSheetView;
import com.example.stafftracker.view.HomeFragment;
import com.example.stafftracker.view.PersonFragment;
import com.google.android.gms.location.FusedLocationProviderClient;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity implements BottomSheetView.BottomSheetListener, NavigationBarView.OnItemSelectedListener {
    final private int REQUEST_CODE_ASK_PERMISSIONS = 1;
    HomeFragment homeFragment = new HomeFragment();
    PersonFragment personFragment = new PersonFragment();
    private FusedLocationProviderClient fusedLocationClient;
    BottomNavigationView bottomNavigationView;
    public Button button;
    TextView textView;
    FloatingActionButton floatingActionButton;
    BottomSheetView bottomSheetView = new BottomSheetView();
    GoogleMap map;
    private int mMenuId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        floatingActionButton = findViewById(R.id.floatingActionButton);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        enableMyLocation();
        checkLocationPermission();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        button.setOnClickListener(v -> getLocation(MainActivity.this));
        floatingActionButton.setOnClickListener(v -> {
            bottomSheetView.show(getSupportFragmentManager(), "bottomSheet");
        });
        loadFragment(
                new HomeFragment()
        );
        bottomNavigationView.setOnItemSelectedListener(this);


    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, fragment)
                    .commit();
            return true;
        }
        return false;
    }



    public void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println(requestCode);
                } else {
                    // Permission Denied
                    Toast.makeText(this, "your message", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    void getLocation(Activity activity) {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            System.out.println();
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        System.out.println();
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                        }
                    });

        }


        }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("destroyed.");
        //
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("Resumed.");
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("Stopped.");
        startService(new Intent(this, BackgroundService.class));
    }

    @Override
    public void onButtonClicked(String text) {
        System.out.println("asdasd");
    }
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (map != null) {
                map.setMyLocationEnabled(true);
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(this, 1,
                    Manifest.permission.ACCESS_COARSE_LOCATION, true);
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
       mMenuId = item.getItemId();
       for(int i=0; i<bottomNavigationView.getMenu().size(); i++){
           MenuItem menuItem = bottomNavigationView.getMenu().getItem(i);
           boolean isChecked = menuItem.getItemId() == item.getItemId();
       }
       switch (item.getItemId()){
           case R.id.home: {
               loadFragment(homeFragment);
           }break;
           case R.id.person:{
               loadFragment(personFragment);
           }break;

           case R.id.settings:{

           }break;
       }
       return true;
    }
}