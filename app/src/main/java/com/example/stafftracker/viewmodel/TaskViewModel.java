package com.example.stafftracker.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.stafftracker.model.Task;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class TaskViewModel extends ViewModel {
    MutableLiveData<ArrayList<Task>> taskMutableLiveData;
    ArrayList<Task> taskArrayList;

    public TaskViewModel(MutableLiveData<ArrayList<Task>> taskMutableLiveData) {
        taskMutableLiveData = new MutableLiveData<>();
    }
}
