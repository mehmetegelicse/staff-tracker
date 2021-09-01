package com.eralpsoftware.stafftracker.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eralpsoftware.stafftracker.MainActivity;
import com.eralpsoftware.stafftracker.model.Company;
import com.example.stafftracker.R;
import com.eralpsoftware.stafftracker.viewmodel.CompanyItemAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class PersonFragment extends Fragment {
    RecyclerView recyclerView;
    protected CompanyItemAdapter adapter;
    ArrayList<Company> companies = new ArrayList<>();
    RecyclerView.LayoutManager mLayoutManager;
    MainActivity mainActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_second, container, false);


        // Inflate the layout for this fragment
        companies.clear();
        getCompanies(view);
        //mainActivity.cardView.setVisibility(View.GONE);

        return view;
    }
    void getCompanies(View view){


        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("companies").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if(queryDocumentSnapshots.isEmpty()){
                System.out.println("List Empty");

            }
            else
            {System.out.println("id : " + queryDocumentSnapshots.size());


                  for (int i=0; i<queryDocumentSnapshots.size(); i++){
                      HashMap<String, Object> latLng = (HashMap<String, Object>) queryDocumentSnapshots.getDocuments().get(i).getData().get("location");

                      if(queryDocumentSnapshots.getDocuments().get(i).get("user").equals(FirebaseAuth.getInstance().getUid())) {
                          companies.add(new Company(queryDocumentSnapshots.getDocuments().get(i).getData().get("name").toString(),
                                  queryDocumentSnapshots.getDocuments().get(i).getData().get("date").toString(),
                                  new LatLng((Double) latLng.get("latitude"), (Double) latLng.get("longitude")),
                                  queryDocumentSnapshots.getDocuments().get(i).getData().get("user").toString(),
                                  queryDocumentSnapshots.getDocuments().get(i).getData().get("description").toString(),
                                  (Double) queryDocumentSnapshots.getDocuments().get(i).getData().get("rating"),
                                  queryDocumentSnapshots.getDocuments().get(i).getData().get("id").toString(),
                                  queryDocumentSnapshots.getDocuments().get(i).getData().get("date").toString())

                          );
                      }
                      adapter.notifyDataSetChanged();


                  }

            }


        });
        adapter = new CompanyItemAdapter(getContext(),companies);
        recyclerView = view.findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager) ;
        recyclerView.setAdapter(adapter);

    }


}
