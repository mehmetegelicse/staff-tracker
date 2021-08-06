package com.example.stafftracker.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import com.example.stafftracker.MainActivity;
import com.example.stafftracker.R;
import com.example.stafftracker.utils.CustomDialog;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import org.jetbrains.annotations.NotNull;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class HomeFragment extends Fragment implements OnMapReadyCallback, BottomSheetView.BottomSheetListener {
    public GoogleMap mMap;
    BottomSheetView bottomSheetView;
    MainActivity mainActivity;
    FusedLocationProviderClient fusedLocationProviderClient;
    FloatingActionButton floatingActionButton;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    LatLng currentLocation;
    LatLng taskPosition;
    String taskMarkerTitle = "";
    String[] task_attr = new String[8];




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {

        mainActivity = (MainActivity) getActivity();
        View view =  inflater.inflate(R.layout.fragment_first,null, false);
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

    private void getlocation(){
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
                            bottomSheetView.show(getChildFragmentManager(), "bottomsheet" );

                        }
                    });

        }
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull @NotNull GoogleMap googleMap) {
        ArrayList<MarkerOptions> markerOptionsArrayList = new ArrayList<>();


        mMap = googleMap;
        LatLng eralp = new LatLng(38.446401, 27.217875);

        mMap.setMyLocationEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(eralp, 13f));
        getCompanies();
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        try {
            if(currentLocation != null){
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> currentLocation = new LatLng(location.getLatitude(), location.getLongitude()));
            }
        }catch (Exception e){
            System.out.println(e);
        }
        mMap.setOnMarkerClickListener(marker -> {

            CustomDialog alert = new CustomDialog();
            if(currentLocation != null && taskPosition != null && marker.isFlat()) {
                alert.showDialog(getActivity(),
                        taskMarkerTitle,
                        currentLocation.latitude,
                        currentLocation.longitude,
                        taskPosition.latitude,
                        taskPosition.longitude,
                        task_attr[3],
                        task_attr[4],
                        task_attr[5],
                        task_attr[6]);
            }
           // else {Toast.makeText(getContext(), "Konuma Ulaşılamıyor.", Toast.LENGTH_SHORT).show();}
            return false;
        });
        getParentFragmentManager().setFragmentResultListener("locationRequest", this, (requestKey, result) -> {

            try {
                task_attr = result.getStringArray("task_location");
                taskMarkerTitle = task_attr[2];
                taskPosition = new LatLng(Double.parseDouble(task_attr[0]), Double.parseDouble(task_attr[1]));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(taskPosition, 14f));
                mMap.addMarker(new MarkerOptions().flat(true).position(taskPosition).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

            }catch (Exception e){
                System.out.println("error: " +e);
            }

        });
        getTasks();

    }

    void getCompanies(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("companies").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if(queryDocumentSnapshots.isEmpty()){
                System.out.println("List Empty");

            }
            else
            {System.out.println("id : " + queryDocumentSnapshots.size());

                for (DocumentSnapshot document: queryDocumentSnapshots) {
                    Map<String, Double> position = (Map<String, Double>) document.get("location");

                    mMap.addMarker(new MarkerOptions().title(document.get("name").toString()).position(new LatLng(position.get("latitude"), position.get("longitude"))));

                }

            }


        });

    }
    void getTasks(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tasks").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if(queryDocumentSnapshots.isEmpty()){
                Toast.makeText(mainActivity, "Hiç bir görev bulunamadı.", Toast.LENGTH_SHORT).show();
            }
            else{
                for (DocumentSnapshot document: queryDocumentSnapshots) {
                    double a =  Double.parseDouble(document.getData().get("latitude").toString());
                    double b = Double.parseDouble(document.getData().get("longitude").toString());
                    mMap.addMarker(new MarkerOptions().title(document.get("title").toString())
                            .position(new LatLng(a,b))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            .flat(true));

                }

            }
        });

    }


    @Override
    public void onButtonClicked(Location location, String text, String description, double rating, String meeting, String meeting_result) {
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(latLng).title(text).icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        addCompanyToDatabase(location,text, description, rating, meeting, meeting_result);
    }
    void addCompanyToDatabase(Location location, String companyName, String description, double rating, String meeting, String meeting_result){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String timeStamp = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss").format(Calendar.getInstance().getTime());
        Map<String, Object> docMap = new HashMap<>();
        docMap.put("user",currentUser.getUid());
        docMap.put("date",timeStamp);
        docMap.put("location", new LatLng(location.getLatitude(), location.getLongitude()));
        docMap.put("name", companyName);
        docMap.put("rating", rating);
        docMap.put("description", description);
        docMap.put("millisTime", System.currentTimeMillis());
        docMap.put("meeting", meeting);
        docMap.put("meeting_result", meeting_result);

        db.collection("companies").add(docMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(mainActivity, companyName +" başarıyla eklendi. "
                        + documentReference.getId(), Toast.LENGTH_SHORT).show();
                db.collection("companies").document(documentReference.getId()).update("id", documentReference.getId());

            }
        });

    }


}