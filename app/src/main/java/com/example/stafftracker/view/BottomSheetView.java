package com.example.stafftracker.view;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.example.stafftracker.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class BottomSheetView extends BottomSheetDialogFragment {
    Geocoder gcd;
    List<Address> addresses;
    private BottomSheetListener mListener;
    Location current_location;
    TextView textView_city, textView_street, tvDateTime;
    FusedLocationProviderClient fusedLocationProviderClient;
    Button button_ekle;
    EditText companyName, companyDescription;
    MaterialRatingBar ratingBar;
    RadioGroup radioGroup;
    RadioButton radio_positive, radio_negative, radio_schedule;
    ConstraintLayout constraintLayout, timeConstraintLayout;
    CalendarView calendarView;
    TimePicker timePicker;
    String date, time;
    String meeting_result = "";
    ImageButton time_back, time_done;
    CardView cardView;
    TextInputLayout textInputLayout_cname;

    boolean completed = false;

    double _rating = -1.0;

    public BottomSheetView( Location location) {
        this.current_location = location;

    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (HomeFragment) getParentFragment();
        } catch(Exception e) {
            //handle exception
        }
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_layout, container, false);
        ratingBar = v.findViewById(R.id.rating_bar);
        companyName = v.findViewById(R.id.et_company);
        companyDescription = v.findViewById(R.id.add_description);
        button_ekle = v.findViewById(R.id.ekle_button);
        radioGroup = v.findViewById(R.id.radioGroup);
        constraintLayout = v.findViewById(R.id.schedule_layout);
        calendarView = v.findViewById(R.id.calendarView);
        calendarView.setMinDate(System.currentTimeMillis()-1000);
        timePicker = v.findViewById(R.id.time_picker);
        timeConstraintLayout = v.findViewById(R.id.time_layout);
        timePicker.setIs24HourView(true);
        time_back = v.findViewById(R.id.time_back);
        time_done = v.findViewById(R.id.time_next);
        tvDateTime = v.findViewById(R.id.textview_date_time);
        cardView = v.findViewById(R.id.date_time_result_container);
        textInputLayout_cname = v.findViewById(R.id.et_company_layout);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.radio_positive:
                        Toast.makeText(getContext(), "positive", Toast.LENGTH_SHORT).show();
                        constraintLayout.setVisibility(View.GONE);
                        cardView.setVisibility(View.GONE);
                        System.out.println(radioGroup.getCheckedRadioButtonId());
                        meeting_result = "Olumlu";

                        break;
                    case R.id.radio_negative:
                        Toast.makeText(getContext(), "negative", Toast.LENGTH_SHORT).show();
                        constraintLayout.setVisibility(View.GONE);
                        cardView.setVisibility(View.GONE);
                        meeting_result = "Olumsuz";



                        break;
                    case R.id.radio_schedule:
                        Toast.makeText(getContext(), "ertele", Toast.LENGTH_SHORT).show();
                        constraintLayout.setVisibility(View.VISIBLE);
                        cardView.setVisibility(View.GONE);
                        System.out.println(radioGroup.getCheckedRadioButtonId());
                        meeting_result = "Ertelendi";
                        return;

                }
            }
        });
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                constraintLayout.setVisibility(View.VISIBLE);
                calendarView.setVisibility(View.VISIBLE);
                timeConstraintLayout.setVisibility(View.GONE);
            }
        });
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                date = dayOfMonth +"/" +month +"/"+ year;
                Toast.makeText(getContext(), dayOfMonth +"/" +month +"/"+ year , Toast.LENGTH_SHORT).show();
                calendarView.setVisibility(View.GONE);
                timeConstraintLayout.setVisibility(View.VISIBLE);
            }
        });
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                time = hourOfDay +":"+minute;
            }
        });
        time_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                constraintLayout.setVisibility(View.VISIBLE);
                timeConstraintLayout.setVisibility(View.GONE);
            }
        });
        time_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                constraintLayout.setVisibility(View.GONE);
                cardView.setVisibility(View.VISIBLE);
                tvDateTime.setText("Randevun: " +  date + ", " + time);
            }
        });
        button_ekle.setOnClickListener(v1 -> {
            boolean companyNameControl = false;
            boolean meetResultControl = false;
            if(meeting_result == null && meeting_result.length()<6){
                meetResultControl = false;


            }else meetResultControl = true;
            if(companyName.getText().toString().isEmpty()){
                textInputLayout_cname.setError("Firma Adı boş bırakılamaz.");
                companyNameControl = false;
            }else companyNameControl =  true;

            if(_rating < 0){
                _rating = 0;
            }
            if(meetResultControl && companyNameControl) {
                mListener.onButtonClicked(current_location, companyName.getText().toString(), companyDescription.getText().toString(), _rating, date + time, meeting_result);
                dismiss();
            }
        });
        ratingBar.setOnRatingChangeListener((ratingBar, rating) -> {
            _rating = rating;
            System.out.println(rating);
        });
        textView_city = v.findViewById(R.id.location_text_city);
        textView_street = v.findViewById(R.id.location_text_street);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        locationRequest(getContext(), current_location);
        return v;
    }



    public interface BottomSheetListener {
        void onButtonClicked(Location location , String text, String description, double rating, String meeting, String meeting_result);
    }



    void locationRequest(Context context, Location location) {
        gcd = new Geocoder(context, Locale.getDefault());
        if (location != null) {
            try {
                addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                System.out.println();


            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addresses != null) {
                textView_street.setText("Adres: " + addresses.get(0).getThoroughfare() + "," + addresses.get(0).getSubThoroughfare() + " " + addresses.get(0).getSubLocality() + "," + addresses.get(0).getSubAdminArea());
                textView_city.setText(addresses.get(0).getAdminArea() + "/" + addresses.get(0).getCountryName());
            }else{
                textView_city.setText("---");
                textView_street.setText("---");
            }
        }

    }
}