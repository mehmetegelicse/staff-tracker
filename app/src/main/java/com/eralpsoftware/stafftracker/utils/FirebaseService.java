package com.eralpsoftware.stafftracker.utils;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.eralpsoftware.stafftracker.model.Task;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FirebaseService {
    private static FirebaseAuth currentUserStatic = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG ="firebase";
    CollectionReference colRef = db.collection("User");
    FirebaseAuth currentUser = FirebaseAuth.getInstance();

    public void setLocationSetting(boolean b) {
        db.collection("user").document(Objects.requireNonNull(currentUser.getUid())).update("location_sharing", b);
    }


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
        if(points.size() >= 10) {
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
    public static ArrayList<Task> fetchTasks(){
        ArrayList<Task> taskArrayList = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tasks").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                    Task task = queryDocumentSnapshots.getDocuments().get(i).toObject(Task.class);
                    taskArrayList.add(task);
                }
            }
        });
        return taskArrayList;
    }
    public static void addCompanyToDatabase(Location location, String companyName, String description, double rating, String meeting, String meeting_result, Activity activity){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String timeStamp = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss").format(Calendar.getInstance().getTime());
        Map<String, Object> docMap = new HashMap<>();
        docMap.put("user",currentUserStatic.getUid());
        docMap.put("date",timeStamp);
        docMap.put("location", new LatLng(location.getLatitude(), location.getLongitude()));
        docMap.put("name", companyName);
        docMap.put("rating", rating);
        docMap.put("description", description);
        docMap.put("millisTime", System.currentTimeMillis());
        docMap.put("meeting", meeting);
        docMap.put("meeting_result", meeting_result);

        db.collection("companies").add(docMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(activity, companyName +" başarıyla eklendi. "
                        + documentReference.getId(), Toast.LENGTH_SHORT).show();
                db.collection("companies").document(documentReference.getId()).update("id", documentReference.getId());

            }
        });

    }

    public void movementHandler(boolean isMoving) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").document(currentUser.getUid()).update("isMoving", isMoving);
    }
}
