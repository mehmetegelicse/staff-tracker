package com.example.stafftracker.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class CheckPermission{
     int LOCATION_PERMISSION_CODE = 1;
     boolean permission = false;




    //  CHECK FOR LOCATION PERMISSION
    public void checkPermission(AppCompatActivity activity){
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED){

        } else {

            requestPermission(activity);

        }
    }

    //REQUEST FOR PERMISSSION
    public void requestPermission(AppCompatActivity activity){

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.ACCESS_BACKGROUND_LOCATION)){
            new AlertDialog.Builder(activity).setTitle("İzin Gerekli.")
                    .setMessage("Uygulamanın düzgün çalışması için konum bilgilerine erişim gerekli.")
                    .setPositiveButton("TAMAM", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},LOCATION_PERMISSION_CODE);
                        }
                    }).setNegativeButton("ENGELLE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                }
            }).create().show();
            Toast.makeText(activity,"GPS permission allows us to access location data. Please allow in App Settings for additional functionality.",Toast.LENGTH_LONG).show();

        } else {


            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},LOCATION_PERMISSION_CODE);
        }
    }


}