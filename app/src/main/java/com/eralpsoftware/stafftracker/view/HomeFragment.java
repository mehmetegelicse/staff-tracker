package com.eralpsoftware.stafftracker.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.eralpsoftware.stafftracker.model.CompanyModel;
import com.eralpsoftware.stafftracker.utils.FirebaseService;
import com.eralpsoftware.stafftracker.MainActivity;
import com.example.stafftracker.R;
import com.eralpsoftware.stafftracker.model.Task;
import com.eralpsoftware.stafftracker.utils.CustomDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class HomeFragment extends Fragment implements OnMapReadyCallback, BottomSheetView.BottomSheetListener {
    public GoogleMap mMap;

    MainActivity mainActivity;
    FusedLocationProviderClient fusedLocationProviderClient;
    FloatingActionButton floatingActionButton;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    LatLng currentLocation;
    String[] task_attr = new String[8];
    ArrayList<CompanyModel> companyModelList = new ArrayList<>();
    int badgeNumber = 0;
    ImageButton closeMap;
    private LatLng taskLocation;


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

        });
        return view;


    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull @NotNull GoogleMap googleMap) {
        ArrayList<MarkerOptions> markerOptionsArrayList = new ArrayList<>();


        mMap = googleMap;
        LatLng eralp = new LatLng(38.446401, 27.217875);

        mMap.setMyLocationEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(eralp, 13f));
       // fetchSavedCompanies();

        mMap.getUiSettings().setScrollGesturesEnabled(true);
        try {
            if(currentLocation == null && mainActivity.gps_enabled && mainActivity.network_enabled){
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> currentLocation = new LatLng(location.getLatitude(), location.getLongitude()));
            }
        }catch (Exception e){
            System.out.println(e);
        }
        if(taskLocation != null){
            mMap.addMarker(new MarkerOptions().position(taskLocation).flat(true));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(taskLocation, 13f));
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
                                    tasks.get(i).getCreatedAt(),
                                    tasks.get(i).getStaffNote());
                        }
                    }
                }

            }
            return false;
        });

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


    @Override
    public void onButtonClicked(Location location, String text, String description, double rating, String meeting, String meeting_result) {
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(latLng).title(text).icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        FirebaseService.addCompanyToDatabase(location,text, description, rating, meeting, meeting_result,getActivity());
    }



}