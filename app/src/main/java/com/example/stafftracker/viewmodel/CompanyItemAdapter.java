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
import com.example.stafftracker.utils.FirebaseService;
import com.google.android.gms.maps.model.LatLng;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class CompanyItemAdapter extends RecyclerView.Adapter<CompanyItemAdapter.ViewHolder> {
    private ArrayList<Company> companyArrayList;
   final private LayoutInflater inflater;
   Context ctx;
    Geocoder gcd;
    List<Address> addresses = new ArrayList<>();



    public CompanyItemAdapter(Context context, ArrayList<Company> companyArrayList) {
        inflater = LayoutInflater.from(context);
        this.companyArrayList = companyArrayList;


    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_company_card, parent, false);
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
    public void onBindViewHolder(@NonNull @NotNull CompanyItemAdapter.ViewHolder holder, int position) {
        LatLng latLng = companyArrayList.get(position).getLocation();

       holder.getCompanyName().setText(companyArrayList.get(position).getName());
       holder.getCompanyAddress().setText(getAddressFromLocation(latLng, ctx));
       holder.getMaterialRatingBar().setRating((float) companyArrayList.get(position).getRating());
       holder.getCompanyDesc().setText(companyArrayList.get(position).getDescription());
       holder.getRemoveButton().setOnClickListener(v -> removeItem(ctx, companyArrayList.get(position).getID(), position).show());
       holder.getCompanyCreatedTime().setText(companyArrayList.get(position).getDate());

    }

    @Override
    public int getItemCount() {
        return companyArrayList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView companyAddress, companyDesc, companyName, companyCreatedTime;

        public Button getRemoveButton() {
            return removeButton;
        }

        Button removeButton;
        MaterialRatingBar materialRatingBar;

        public TextView getCompanyAddress() {
            return companyAddress;
        }

        public TextView getCompanyDesc() {
            return companyDesc;
        }

        public TextView getCompanyName() {
            return companyName;
        }

        public TextView getCompanyCreatedTime() {
            return companyCreatedTime;
        }

        public MaterialRatingBar getMaterialRatingBar() {
            return materialRatingBar;
        }

        public void setMaterialRatingBar(MaterialRatingBar materialRatingBar) {
            this.materialRatingBar = materialRatingBar;
        }

        public ViewHolder(@NonNull @NotNull View view) {
            super(view);
            materialRatingBar = view.findViewById(R.id.card_rating_bar);
            companyName = view.findViewById(R.id.company_name);
            companyDesc = view.findViewById(R.id.company_description);
            companyAddress = view.findViewById(R.id.company_address);
            removeButton = view.findViewById(R.id.remove_company);
            materialRatingBar.setEnabled(false);
            companyCreatedTime = view.findViewById(R.id.c_cretated_time_adapter);




        }

    }
     AlertDialog removeItem(Context context, String id, int position){ AlertDialog alertDialog = new AlertDialog.Builder(context).setTitle("Uyarı").setMessage("Kaydettiğiniz yeri silmek istediğinize emin misiniz?")
                .setNegativeButton("iptal", (dialog, which) -> System.out.println("iptal")).setPositiveButton("Tamam", (dialog, which) ->
             {
                 FirebaseService.removeCompany("companies", id);
                 Toast.makeText(context, "id : " + id, Toast.LENGTH_SHORT).show();
                 companyArrayList.remove(position);
                 notifyDataSetChanged();
             }
            ).create();
     return alertDialog;
    }


}