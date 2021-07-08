package com.example.stafftracker.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.stafftracker.LoginActivity;
import com.example.stafftracker.MainActivity;
import com.example.stafftracker.R;
import com.example.stafftracker.utils.BackgroundService;
import com.example.stafftracker.utils.FirebaseService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.function.Function;

public class HomeFragment extends Fragment implements OnMapReadyCallback{
    GoogleMap mMap;
    ArrayList<LatLng> points = new ArrayList<>();
    MainActivity mainActivity;
    FirebaseService fb = new FirebaseService();
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    FusedLocationProviderClient fusedLocationClient;
    final int LOCATION_GETTING_PERIOD = 5000;  // 5 seconds
    final int MAX_ARRAY_SIZE = 100;
    private Handler mHandler;
    boolean onLoop =  false;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {

        mainActivity = (MainActivity) getActivity();
        View view =  inflater.inflate(R.layout.fragment_first,null, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mainActivity.button.setText("START");
        mainActivity.button.setBackgroundColor(Color.GREEN);
        mainActivity.button.setOnClickListener(v ->{

            mainActivity.button.setText("STOP");
            mainActivity.button.setBackgroundColor(Color.RED);
            if(!onLoop){
            mHandler = new Handler();
            startRepeatingTask();
            onLoop  = !onLoop;
            }
            else stopRepeatingTask();
                }
        );


        return view;


    }
    Runnable mStatusChecker = new Runnable() {
        @SuppressLint("MissingPermission")
        @Override
        public void run() {

            try {
            } finally {

                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(getActivity(), location -> {
                            if (location != null) {
                                System.out.println(location.getLatitude());
                                points.add(new LatLng(location.getLatitude(),location.getLongitude()));
                            }
                            if(points.size() > MAX_ARRAY_SIZE){
                                String timeStamp = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss").format(Calendar.getInstance().getTime());

                                fb.pushFiles(points,currentUser.getUid(),timeStamp);
                            points.clear();
                            }
                        });
                mHandler.postDelayed(mStatusChecker, LOCATION_GETTING_PERIOD);
            }
        }

    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull @NotNull GoogleMap googleMap) {

        mMap = googleMap;
        LatLng eralp = new LatLng(38.446401, 27.217875);
        MarkerOptions markerOptions = new MarkerOptions().position(eralp).title("Mehmet Egeli").snippet("EralpSoftware");
        Marker marker = mMap.addMarker(markerOptions);
        marker.showInfoWindow();
        mMap.setMyLocationEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(eralp, 15f));

    }

}