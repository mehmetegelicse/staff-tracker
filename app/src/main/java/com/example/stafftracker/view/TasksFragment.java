package com.example.stafftracker.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.stafftracker.MainActivity;
import com.example.stafftracker.R;
import com.example.stafftracker.model.Company;
import com.example.stafftracker.model.Task;
import com.example.stafftracker.viewmodel.CompanyItemAdapter;
import com.example.stafftracker.viewmodel.TaskItemAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TasksFragment extends Fragment implements TaskItemAdapter.ITaskLocation {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    TaskItemAdapter taskItemAdapter;
    MainActivity mainActivity;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    ArrayList<Task> taskArrayList = new ArrayList<>();
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    HomeFragment homeFragment = new HomeFragment();


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TasksFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TasksFragment newInstance(String param1, String param2) {
        TasksFragment fragment = new TasksFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        getParentFragmentManager().setFragmentResultListener("request", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull @NotNull String requestKey, @NonNull @NotNull Bundle result) {
                System.out.println(result.getString("key"));
            }
        });
        mainActivity = (MainActivity) getActivity();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_tasks, container, false);
        // Inflate the layout for this fragment
        loadAdapter(v);
        taskArrayList.clear();
        mainActivity.cardView.setVisibility(View.GONE);
        return v;
    }
    void loadAdapter(View view){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tasks").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots.isEmpty()){
                    System.out.println("List Empty");

                }
                else
                {System.out.println("id : " + queryDocumentSnapshots.size());


                    for (int i=0; i<queryDocumentSnapshots.size(); i++){
                        if(queryDocumentSnapshots.getDocuments().get(i).get("userId").equals(firebaseAuth.getUid())) {
                            taskArrayList.add(new Task(
                                    queryDocumentSnapshots.getDocuments().get(i).getData().get("id").toString(),
                                    queryDocumentSnapshots.getDocuments().get(i).getData().get("userId").toString(),
                                    queryDocumentSnapshots.getDocuments().get(i).getData().get("title").toString(),
                                    (long)queryDocumentSnapshots.getDocuments().get(i).getData().get("createdAt"),
                                    queryDocumentSnapshots.getDocuments().get(i).getData().get("description").toString(),
                                    Integer.parseInt(queryDocumentSnapshots.getDocuments().get(i).getData().get("status").toString()),
                                    Double.parseDouble( queryDocumentSnapshots.getDocuments().get(i).getData().get("latitude").toString()),
                                    Double.parseDouble(  queryDocumentSnapshots.getDocuments().get(i).getData().get("longitude").toString()),
                                    queryDocumentSnapshots.getDocuments().get(i).getData().get("staffNote").toString()

                            ));
                        }
                        taskItemAdapter.notifyDataSetChanged();


                    }

                }
            }
        });
        //dString id,
        //            String userId,
        //            String title,
        //            long created,
        //            String description,
        //            int status,
        //            double latitude,
        //            double longitude


        taskItemAdapter = new TaskItemAdapter(getContext(),taskArrayList,this );
        recyclerView = view.findViewById(R.id.task_recyclerview);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager) ;
        recyclerView.setAdapter(taskItemAdapter);
    }

    @Override
    //double latitude, double longitude, String title, int status, String description, long createdAt,string TaskNote
    public void showTaskLocation(String id,double latitude, double longitude, String title, int status, String description, long createdAt, String taskNote) {
        Bundle result = new Bundle();
        String[] taskLocation = {latitude+"", longitude+"", title, status +"", description, createdAt +"", taskNote, id};
        result.putStringArray("task_location", taskLocation);
        getParentFragmentManager().setFragmentResult("locationRequest", result);
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, homeFragment)
                .commit();

    }
}