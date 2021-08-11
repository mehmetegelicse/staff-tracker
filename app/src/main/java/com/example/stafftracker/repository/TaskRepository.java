package com.example.stafftracker.repository;

import android.app.Application;

import androidx.lifecycle.MutableLiveData;

import com.example.stafftracker.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskRepository {
    ArrayList<Task> tasks = new ArrayList<>();
    private MutableLiveData<List<Task>> mutableLiveData = new MutableLiveData<>();
    private Application application;

    public TaskRepository(Application application) {
        this.application = application;
    }
}
