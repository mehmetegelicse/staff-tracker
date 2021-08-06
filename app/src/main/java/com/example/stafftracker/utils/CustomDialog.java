package com.example.stafftracker.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.example.stafftracker.R;

import java.text.DateFormat;

public class CustomDialog {
    public void showDialog(Activity activity, String msg,
                           double latitude,
                           double longitude,
                           double t_latitude,
                           double t_longitude,
                           String status,
                           String description,
                           String createdAt,
                           String task_note
    ){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog);

        TextView text = dialog.findViewById(R.id.text_dialog);
        TextView createdTimeDialog = dialog.findViewById(R.id.created_time_dialog);
        TextView taskDescription = dialog.findViewById(R.id.dialog_task_description);
        TextView task_status = dialog.findViewById(R.id.dialog_status);
        TextView task_note_tv = dialog.findViewById(R.id.dialog_task_note);

        Button directionButton = dialog.findViewById(R.id.direction_button);
        Button dialogButton = dialog.findViewById(R.id.btn_dialog);
        Button streetViewButton = dialog.findViewById(R.id.street_view_button);


        text.setText(msg);
        task_note_tv.setText(task_note);
        createdTimeDialog.setText(DateFormat.getDateInstance(DateFormat.SHORT).format(Long.parseLong(createdAt)));
        taskDescription.setText(description);
        task_status.setText(taskStatusMapper(Integer.parseInt(status)));


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