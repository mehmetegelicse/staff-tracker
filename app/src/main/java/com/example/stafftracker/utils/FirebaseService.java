package com.example.stafftracker.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class FirebaseService {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG ="firebase";
    CollectionReference colRef = db.collection("User");


    public void pushFiles(ArrayList<LatLng> points, String userId, String time){

        Collections.addAll(points);

        Map<String, Object> docData = new HashMap<>();
        docData.put("id", userId);
        docData.put("time", time);
        docData.put("locations", points);


        CollectionReference locations = db.collection("locations");
        try{
            locations.add(docData);
        }catch (Exception e){
            System.out.println(e);
        }

    }
    public void addField(ArrayList<LatLng> points ){
        String timeStamp = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss").format(Calendar.getInstance().getTime());
        db.collection("User").document().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    WriteBatch writeBatch = db.batch();

                   DocumentSnapshot documentSnapshot = task.getResult();
                   DocumentReference documentReference = documentSnapshot.getReference();
                   Map<String, Object> new_map = new HashMap<>();
                   new_map.put(timeStamp, points);
                   writeBatch.update(documentReference, new_map);
                   writeBatch.commit();
                }else System.out.println("ERROR!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // ... "Failure getting documents -> " + e
            }
        });
    }

}
