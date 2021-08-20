package com.eralpsoftware.stafftracker.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.eralpsoftware.stafftracker.MainActivity;
import com.example.stafftracker.R;
import com.eralpsoftware.stafftracker.model.Task;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.ArrayList;

public class CustomDialog {
    public void showDialog(Activity activity, String msg,
                           double latitude,
                           double longitude,
                           double t_latitude,
                           double t_longitude,
                           int status,
                           String description,
                           long createdAt,
                           String task_note
    ){
        MainActivity mainActivity = (MainActivity) activity;
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog);
        ArrayList<Task> tasksArrayList = mainActivity.getTasks();

        TextView text = dialog.findViewById(R.id.text_dialog);
        TextView createdTimeDialog = dialog.findViewById(R.id.created_time_dialog);
        TextView taskDescription = dialog.findViewById(R.id.dialog_task_description);
        Spinner task_status = dialog.findViewById(R.id.task_state_spinner_alert);
        TextView task_note_tv = dialog.findViewById(R.id.dialog_task_note);

        Button directionButton = dialog.findViewById(R.id.direction_button);
        Button dialogButton = dialog.findViewById(R.id.btn_dialog);
        Button streetViewButton = dialog.findViewById(R.id.street_view_button);


        text.setText(msg);
        task_note_tv.setText(task_note);
        createdTimeDialog.setText(DateFormat.getDateInstance(DateFormat.SHORT).format(createdAt));
        taskDescription.setText(description);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(dialog.getContext(), R.array.task_states, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        task_status.setAdapter(adapter);
        task_status.setSelection(status);
        task_status.setBackgroundResource(android.R.color.holo_blue_light);
        task_status.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(id != status ) {
                    for (int i = 0; i < tasksArrayList.size(); i++) {
                        if (t_latitude == tasksArrayList.get(i).getLatitude() && t_longitude == tasksArrayList.get(i).getLongitude()) {
                            tasksArrayList.get(i).setStatus((int) id);
                            changeStatus(tasksArrayList.get(i).getId(), (int) id, dialog.getContext());
                        }
                    }
                    Toast.makeText(dialog.getContext(), id + "", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });

       streetViewButton.setOnClickListener(v -> {
           Uri uri = Uri.parse( "http://maps.google.com/maps?q=&layer=c&cbll="
                 +t_latitude+","+t_longitude);
           Intent intent = new Intent(Intent.ACTION_VIEW, uri);
           activity.startActivity(intent);
       });
        dialogButton.setOnClickListener(v -> dialog.dismiss());


        directionButton.setOnClickListener(v -> {
            Uri uri = Uri.parse( "https://www.google.com/maps/dir/" + latitude +","+ longitude +
                    "/"+t_latitude+","+t_longitude);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            activity.startActivity(intent);
        });
        dialog.show();

    }
    void changeStatus(String task_id, int status, Context ctx){
        if(task_id != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("tasks").document(task_id).update("status", status).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(ctx, "Durum değiştirildi.", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else Toast.makeText(ctx, "Hata Oluştu.", Toast.LENGTH_SHORT).show();
    }
    String taskStatusMapper(int status){
        String task_status = "";
        switch (status){
            case 0:
                task_status = "Beklemede";
                break;
            case 1:
                task_status = "Tamamlandı";
                break;
            case 2:
                task_status = "Başka Bir durum..";
                break;
        }
        return task_status;
    }
}