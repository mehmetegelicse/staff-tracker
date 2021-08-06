package com.example.stafftracker.viewmodel;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stafftracker.R;
import com.example.stafftracker.model.Company;
import com.example.stafftracker.model.Task;
import com.example.stafftracker.utils.FirebaseService;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class TaskItemAdapter extends RecyclerView.Adapter<TaskItemAdapter.ViewHolder> {
    private ArrayList<Task> tasks;
    final private LayoutInflater inflater;
    Context ctx;
    Geocoder gcd;
    List<Address> addresses = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();



    public TaskItemAdapter(Context context, ArrayList<Task> tasks, ITaskLocation callback) {
        inflater = LayoutInflater.from(context);
        this.tasks = tasks;
        mCallback = callback;


    }
    public interface ITaskLocation {
        void showTaskLocation(String id, double latitude, double longitude, String title, int status, String description, long createdAt, String taskName);
    }
    private ITaskLocation mCallback;

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_item_card, parent, false);
        ctx = parent.getContext();
        return new ViewHolder(view);
    }
    public String getAddressFromLocation(LatLng latLng, Context context){
        String adres = "";
        gcd = new Geocoder(context, Locale.getDefault());
        try {
            addresses = gcd.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {

                adres =  ( addresses.get(0).getThoroughfare() + "," + addresses.get(0).getSubThoroughfare() + " "
                        + addresses.get(0).getSubLocality() + "," + addresses.get(0).getSubAdminArea() + "\n" +
                        addresses.get(0).getAdminArea() + "/" + addresses.get(0).getCountryName());
            }else{return  "";}
        } catch (IOException e) {
            e.printStackTrace();
        }

        return adres;
    }



    @Override
    public void onBindViewHolder(@NonNull @NotNull TaskItemAdapter.ViewHolder holder, int position) {

        holder.getTaskName().setText(tasks.get(position).getTitle());
        holder.getAddress().setText(getAddressFromLocation( new LatLng(tasks.get(position).getLatitude(), tasks.get(position).getLongitude()), ctx));
        holder.getDescription().setText(tasks.get(position).getDescription());
        holder.getTime().setText(DateFormat.getDateInstance(DateFormat.SHORT).format(tasks.get(position).getCreated()));
        holder.getStatus().setText(taskStatusMapper(tasks.get(position).getStatus()));
        holder.getAddNote().setOnClickListener(v -> {

        });
        holder.getTaskNote().setText(tasks.get(position).getStaffNote());
        holder.getShowLocation().setOnClickListener(v ->
                mCallback.showTaskLocation( tasks.get(position).getId(),
                                            tasks.get(position).getLatitude(),
                                            tasks.get(position).getLongitude(),
                                            tasks.get(position).getTitle(),
                                            tasks.get(position).getStatus(),
                                            tasks.get(position).getDescription(),
                                            tasks.get(position).getCreated(),
                                            tasks.get(position).getStaffNote()));
        holder.getAddNote().setOnClickListener(v -> {
            final EditText taskEditText = new EditText(ctx);
            AlertDialog dialog = new AlertDialog.Builder(ctx)
                    .setTitle(tasks.get(position).getTitle())
                    .setMessage("Göreve Açıklama Ekle")
                    .setView(taskEditText)
                    .setPositiveButton("ekle", (dialog1, which) -> {
                        String taskNote = String.valueOf(taskEditText.getText());
                        try {
                            db.collection("tasks").document(tasks.get(position).getId()).
                                    update("staffNote", taskNote).
                                    addOnSuccessListener(unused -> {
                                        tasks.get(position).setStaffNote(taskNote);
                                        notifyDataSetChanged();
                                    });

                        }catch (Exception e){
                            System.out.println("update error : " + e);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create();
            dialog.show();
        });
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

    @Override
    public int getItemCount() {
        return tasks.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView taskName;
        TextView title;
        TextView address;
        TextView time;
        TextView status;
        TextView description;
        TextView taskNote;

        public TextView getTaskNote() {
            return taskNote;
        }

        Button addNote, showLocation;


        public Button getAddNote() {
            return addNote;
        }

        public Button getShowLocation() {
            return showLocation;
        }

        public TextView getTaskName() {
            return taskName;
        }

        public TextView getTitle() {
            return title;
        }

        public TextView getAddress() {
            return address;
        }

        public TextView getTime() {
            return time;
        }

        public TextView getStatus() {
            return status;
        }

        public TextView getDescription() {
            return description;
        }




        public ViewHolder(@NonNull @NotNull View view) {

            super(view);
    taskName = view.findViewById(R.id.task_name);
    time = view.findViewById(R.id.task_created_time);
    description = view.findViewById(R.id.task_description);
    address = view.findViewById(R.id.company_address_task);
    status = view.findViewById(R.id.task_status);
    addNote = view.findViewById(R.id.add_note);
    showLocation = view.findViewById(R.id.location_button);
    taskNote = view.findViewById(R.id.task_note);


        }

    }
    AlertDialog removeItem(Context context, String id, int position){ AlertDialog alertDialog = new AlertDialog.Builder(context).setTitle("Uyarı").setMessage("Kaydettiğiniz yeri silmek istediğinize emin misiniz?")
            .setNegativeButton("iptal", (dialog, which) -> System.out.println("iptal")).setPositiveButton("Tamam", (dialog, which) ->
                    {
                        FirebaseService.removeCompany("companies", id);
                        Toast.makeText(context, "id : " + id, Toast.LENGTH_SHORT).show();
                        tasks.remove(position);
                        notifyDataSetChanged();
                    }
            ).create();
        return alertDialog;
    }



}