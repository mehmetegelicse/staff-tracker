package com.eralpsoftware.stafftracker.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.eralpsoftware.stafftracker.model.Task;

import java.util.ArrayList;

public class TaskViewModel extends ViewModel {
    MutableLiveData<ArrayList<Task>> taskMutableLiveData;
    ArrayList<Task> taskArrayList;

    public TaskViewModel(MutableLiveData<ArrayList<Task>> taskMutableLiveData) {
        taskMutableLiveData = new MutableLiveData<>();
    }
}
