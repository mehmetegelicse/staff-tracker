package com.example.stafftracker.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.stafftracker.MainActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static android.content.ContentValues.TAG;

public class FirebaseService {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG ="firebase";
    CollectionReference colRef = db.collection("User");
    FirebaseAuth currentUser = FirebaseAuth.getInstance();


    public void pushFiles(Context ctx,ArrayList<Map<String, Object>> points, String userId, String time){
        Collections.addAll(points);

        Map<String, Object> docData = new HashMap<>();
        docData.put("id", userId);
        docData.put("time", time);
        docData.put("locations", points);
        docData.put("millisTime", System.currentTimeMillis());


        CollectionReference locations = db.collection("locations");
        Map<String, Object> data = new HashMap<>();
        data.put("last_seen", points.get(points.size()-1));
        db.collection("user").document(Objects.requireNonNull(currentUser.getUid())).set(data, SetOptions.merge());
        if(points.size() > 20) {
            try {
                locations.add(docData).addOnSuccessListener(documentReference ->
                        Toast.makeText(ctx, points.size() + " location points sent to database.", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                System.out.println(e);
            }
        }

    }
    public void addField(ArrayList<LatLng> points ){
        String timeStamp = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss").format(Calendar.getInstance().getTime());
        db.collection("User").document().get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                WriteBatch writeBatch = db.batch();

               DocumentSnapshot documentSnapshot = task.getResult();
               DocumentReference documentReference = documentSnapshot.getReference();
               Map<String, Object> new_map = new HashMap<>();
               new_map.put(timeStamp, points);
               writeBatch.update(documentReference, new_map);
               writeBatch.commit();
            }else System.out.println("ERROR!");
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
    public static void removeCompany(String collection,String id){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(collection).document(id).delete();
    }

}
