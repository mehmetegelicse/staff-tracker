package com.eralpsoftware.stafftracker.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eralpsoftware.stafftracker.MainActivity;
import com.example.stafftracker.R;
import com.eralpsoftware.stafftracker.model.Task;
import com.eralpsoftware.stafftracker.viewmodel.TaskItemAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

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
    HomeFragment homeFragment = new HomeFragment();
    MainActivity mainActivity;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    ArrayList<Task> taskArrayList;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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
        if(mainActivity.tasks != null) {
            mainActivity.getBottomNavigationView().getOrCreateBadge(R.id.tasks).setNumber(mainActivity.tasks.size());
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_tasks, container, false);
        // Inflate the layout for this fragment
        loadAdapter(v);
        mainActivity.cardView.setVisibility(View.GONE);
        return v;
    }
    void loadAdapter(View view){
        taskItemAdapter = new TaskItemAdapter(getContext(), mainActivity.getTasks(), this );
        recyclerView = view.findViewById(R.id.task_recyclerview);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager) ;
        recyclerView.setAdapter(taskItemAdapter);
    }

    @Override
    //double latitude, double longitude, String title, int status, String description, long createdAt,string TaskNote
    public void showTaskLocation(String id,double latitude, double longitude, String title, int status, String description, long createdAt, String taskNote) {
        Bundle result = new Bundle();
        String[] taskLocation = {latitude+"", longitude+""};
        result.putStringArray("task_location", taskLocation);
        getParentFragmentManager().setFragmentResult("locationRequest", result);
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, mainActivity.homeFragment)
                .commit();

    }
}