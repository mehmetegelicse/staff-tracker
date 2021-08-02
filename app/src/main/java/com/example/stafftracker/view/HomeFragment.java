package com.example.stafftracker.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class HomeFragment extends Fragment implements OnMapReadyCallback, BottomSheetView.BottomSheetListener {
    public GoogleMap mMap;
    BottomSheetView bottomSheetView;
    MainActivity mainActivity;
    FusedLocationProviderClient fusedLocationProviderClient;
    FloatingActionButton floatingActionButton, testFloatingactionButton;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();



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
        testFloatingactionButton = view.findViewById(R.id.testfloatingActionButton);
        testFloatingactionButton.setOnClickListener(v -> {
            System.out.println("test basıldı.");
            Bundle result = new Bundle();
            result.putString("key", "test");
            getParentFragmentManager().setFragmentResult("request", result);
        });
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
                            bottomSheetView = new BottomSheetView( location);
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




    }

    void getCompanies(){

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("companies").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {

            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
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
        DocumentReference ref = db.collection("companies").document();
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