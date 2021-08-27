package com.eralpsoftware.stafftracker.view;

import static com.eralpsoftware.stafftracker.utils.Utils.BACKGROUND_KEY;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.eralpsoftware.stafftracker.MainActivity;
import com.eralpsoftware.stafftracker.utils.FirebaseService;
import com.eralpsoftware.stafftracker.utils.PreferencesHelper;
import com.eralpsoftware.stafftracker.utils.ScanService;
import com.eralpsoftware.stafftracker.utils.Utils;
import com.example.stafftracker.R;
import com.eralpsoftware.stafftracker.SplashScreen;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button logoutButton;
    FirebaseAuth firebaseAuth;
    FirebaseService fs = new FirebaseService();
    SwitchMaterial switchMaterial;
    MainActivity mainActivity;


    public SettingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingFragment newInstance(String param1, String param2) {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_setting, container, false);
        mainActivity = (MainActivity) getActivity();
        logoutButton = v.findViewById(R.id.logoutButton);
        firebaseAuth = FirebaseAuth.getInstance();
        logoutButton.setOnClickListener(v1 -> new AlertDialog.Builder(getContext())
                .setTitle("Çıkış")
                .setMessage("Mevcut oturumunuz sonlandırılsın mı?")
                .setPositiveButton("ÇIKIŞ YAP", (paramDialogInterface, paramInt) -> {
                    FirebaseUser user =  firebaseAuth.getCurrentUser();
                    firebaseAuth.signOut();
                    PreferencesHelper.getInstance(getContext()).clearCache();
                    user = firebaseAuth.getCurrentUser();
                    Intent i = new Intent(getContext(), SplashScreen.class);
                    getActivity().finish();


                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//makesure user cant go back
                    startActivity(i);
                })
                .setNegativeButton("iptal",null)
                .show());
        switchMaterial = v.findViewById(R.id.switch_setting);
        switchMaterial.setChecked(PreferencesHelper.getInstance(getContext()).readData(BACKGROUND_KEY));
        switchMaterial.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                PreferencesHelper.getInstance(getContext()).writeData(BACKGROUND_KEY, true);
                changeActivityView();
                Toast.makeText(getContext(), "Konum paylaşılacak", Toast.LENGTH_SHORT).show();
                fs.setLocationSetting(true);
            }else{
                PreferencesHelper.getInstance(getContext()).writeData(BACKGROUND_KEY,false);
                mainActivity.stopService(new Intent(getContext(), ScanService.class));
                changeActivityView();
                fs.setLocationSetting(false);
            }

        });

        return  v;
    }
    void changeActivityView(){
        if(!PreferencesHelper.getInstance(getContext()).readData(BACKGROUND_KEY)){
            mainActivity.button.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.location_track_red));
            mainActivity.open_close.setText(getString(R.string.background_closed));
        }
        else {mainActivity.button.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.track_location));
            if(mainActivity.isMyServiceRunning(ScanService.class)){
                mainActivity.open_close.setText(getString(R.string.background_open));
            }else {
                mainActivity.open_close.setText(getString(R.string.ready));
            }
        }

    }
}