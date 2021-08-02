package com.example.stafftracker.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.stafftracker.R;
import com.example.stafftracker.model.Task;
import com.example.stafftracker.viewmodel.CompanyItemAdapter;
import com.example.stafftracker.viewmodel.TaskItemAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TasksFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    TaskItemAdapter taskItemAdapter;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    ArrayList<Task> taskArrayList = new ArrayList<>();


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
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_tasks, container, false);
        // Inflate the layout for this fragment
        loadAdapter(v);

        return v;
    }
    void loadAdapter(View view){
        //dString id,
        //            String userId,
        //            String title,
        //            long created,
        //            String description,
        //            int status,
        //            double latitude,
        //            double longitude

        taskArrayList.add(new Task("1", "task1", "başlık firması", System.currentTimeMillis(),"görüşme yapılacak", 1, 27.33,38.23));
        taskArrayList.add(new Task("2", "task2", "başlık restoranı", System.currentTimeMillis(),"görüşme yapılacak", 2, 27.34,38.233));

        taskItemAdapter = new TaskItemAdapter(getContext(),taskArrayList);
        recyclerView = view.findViewById(R.id.task_recyclerview);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager) ;
        recyclerView.setAdapter(taskItemAdapter);
    }

}