package com.example.stafftracker.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.stafftracker.MainActivity;
import com.example.stafftracker.R;
import com.example.stafftracker.model.CompanyModel;
import com.example.stafftracker.model.Task;
import com.example.stafftracker.utils.CustomDialog;
import com.example.stafftracker.utils.FirebaseService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;


public class HomeFragment extends Fragment implements OnMapReadyCallback, BottomSheetView.BottomSheetListener {
    public GoogleMap mMap;
    BottomSheetView bottomSheetView;
    MainActivity mainActivity;
    FusedLocationProviderClient fusedLocationProviderClient;
    FloatingActionButton floatingActionButton;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    LatLng currentLocation;
    String[] task_attr = new String[8];
    ArrayList<CompanyModel> companyModelList = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {

        mainActivity = (MainActivity) getActivity();
        View view = inflater.inflate(R.layout.fragment_first, null, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        floatingActionButton = view.findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(v -> {
            getlocation();
        });
        mainActivity.cardView.setVisibility(View.VISIBLE);
        return view;


    }

    private void getlocation() {
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            System.out.println();
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        System.out.println();
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            bottomSheetView = new BottomSheetView(location);
                            bottomSheetView.show(getChildFragmentManager(), "bottomsheet");

                        }
                    });

        }
    }

    @Override
    public void onMapReady(@NonNull @NotNull GoogleMap googleMap) {
        ArrayList<MarkerOptions> markerOptionsArrayList = new ArrayList<>();


        mMap = googleMap;
        LatLng eralp = new LatLng(38.446401, 27.217875);

        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(eralp, 13f));
        fetchSavedCompanies();
        fetchTasks();
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        try {
            if(currentLocation == null){
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> currentLocation = new LatLng(location.getLatitude(), location.getLongitude()));
            }
        }catch (Exception e){
            System.out.println(e);
        }

        mMap.setOnMarkerClickListener(marker -> {
            ArrayList<Task> tasks =  new ArrayList<>();
            tasks = mainActivity.getTasks();
            CustomDialog alert = new CustomDialog();
            if(currentLocation != null && marker.isFlat()) {
                for (int i = 0; i < tasks.size(); i++) {
                    if(!tasks.isEmpty()){
                        if(marker.getPosition().latitude == tasks.get(i).getLatitude() &&
                                marker.getPosition().longitude == tasks.get(i).getLongitude()){
                            alert.showDialog(getActivity(),
                                    tasks.get(i).getTitle(),
                                    currentLocation.latitude,
                                    currentLocation.longitude,
                                    marker.getPosition().latitude,
                                    marker.getPosition().longitude,
                                    tasks.get(i).getStatus(),
                                    tasks.get(i).getDescription(),
                                    tasks.get(i).getCreated(),
                                    tasks.get(i).getStaffNote());
                        }
                    }
                }

            }
            return false;
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mainActivity.getBottomNavigationView().setBackground(mainActivity.getDrawable(R.drawable.gradient_card));
        }
        getParentFragmentManager().setFragmentResultListener("locationRequest",this, (requestKey, result) ->
        {
            try {
                task_attr = result.getStringArray("task_location");
                LatLng taskPosition = new LatLng(Double.parseDouble(task_attr[0]), Double.parseDouble(task_attr[1]));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(taskPosition,14f));
            }catch (Exception e){
                System.out.println(".onMapReady" + e);
            }
        }      );
    }
    void fetchSavedCompanies(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("companies")
                .orderBy("date",Query.Direction.DESCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots.isEmpty()){
                    Toast.makeText(getContext(), "Kaydedilmiş Yer Yok.", Toast.LENGTH_SHORT).show();
                }
                for (DocumentSnapshot snapshot:queryDocumentSnapshots
                     ) {
                   companyModelList = (ArrayList<CompanyModel>) queryDocumentSnapshots.toObjects(CompanyModel.class);


                }
                for (int i = 0; i < companyModelList.size(); i++) {
                    if (companyModelList.get(i).getUser().equals(currentUser.getUid())){

                        companyModelList.remove(i);

                    }
                }
                for (int i = 0; i < companyModelList.size(); i++) {
                    mMap.addMarker(new MarkerOptions().title(companyModelList.get(i).getName())
                            .position(new LatLng(companyModelList.get(i).getLocation().get("latitude"), companyModelList.get(i).getLocation().get("longitude"))));

                }
            }
        });

    }

    public ArrayList<Task> fetchTasks(){
        ArrayList<Task> taskArrayList = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tasks").
                orderBy("createdAt", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                    com.example.stafftracker.model.Task task = queryDocumentSnapshots.getDocuments().get(i).toObject(Task.class);
                    if(task.getUserId().equals(currentUser.getUid())) {
                        task.setCreated((long) queryDocumentSnapshots.getDocuments().get(i).get("createdAt"));
                        taskArrayList.add(task);

                    }
                    double a = task.getLatitude();
                    double b = task.getLongitude();
                    try {
                        if(task.getUserId().equals(currentUser.getUid())) {
                            if (task.getStatus() == 1) {
                                mMap.addMarker(new MarkerOptions().title(task.getTitle())
                                        .position(new LatLng(a, b))
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                        .flat(true));
                            }
                            mMap.addMarker(new MarkerOptions().title(task.getTitle())
                                    .position(new LatLng(a, b))
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                    .flat(true));

                        }
                    }catch (Exception e){
                        System.out.printf("hata : "+ e);
                    }
                }
                mainActivity.setTasks(taskArrayList);
                mainActivity.getBottomNavigationView().getOrCreateBadge(R.id.tasks).setNumber(taskArrayList.size());
            }
        });
        return taskArrayList;
    }
    @Override
    public void onButtonClicked(Location location, String text, String description, double rating, String meeting, String meeting_result) {
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(latLng).title(text).icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        FirebaseService.addCompanyToDatabase(location,text, description, rating, meeting, meeting_result,getActivity());
    }




}