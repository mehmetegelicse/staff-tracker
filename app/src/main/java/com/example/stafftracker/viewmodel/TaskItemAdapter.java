package com.example.stafftracker.viewmodel;

import android.app.AlertDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stafftracker.R;
import com.example.stafftracker.model.Company;
import com.example.stafftracker.model.Task;
import com.example.stafftracker.utils.FirebaseService;
import com.google.android.gms.maps.model.LatLng;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.io.IOException;
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



    public TaskItemAdapter(Context context, ArrayList<Task> tasks) {
        inflater = LayoutInflater.from(context);
        this.tasks = tasks;


    }

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

                adres =  ("Adres: " + addresses.get(0).getThoroughfare() + "," + addresses.get(0).getSubThoroughfare() + " "
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

    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView getTaskName() {
            return taskName;
        }

        TextView taskName;
        public ViewHolder(@NonNull @NotNull View view) {

            super(view);
    taskName = view.findViewById(R.id.task_name);
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