package com.eralpsoftware.stafftracker;

import static com.eralpsoftware.stafftracker.utils.Utils.BACKGROUND_KEY;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.eralpsoftware.stafftracker.utils.FirebaseService;
import com.eralpsoftware.stafftracker.utils.PreferencesHelper;
import com.eralpsoftware.stafftracker.utils.Utils;
import com.eralpsoftware.stafftracker.view.HomeFragment;
import com.eralpsoftware.stafftracker.view.SettingFragment;
import com.example.stafftracker.R;
import com.eralpsoftware.stafftracker.model.Task;
import com.eralpsoftware.stafftracker.utils.ScanService;
import com.eralpsoftware.stafftracker.view.PersonFragment;
import com.eralpsoftware.stafftracker.view.TasksFragment;
import com.google.android.gms.location.FusedLocationProviderClient;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import org.jetbrains.annotations.NotNull;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    final private int REQUEST_CODE_ASK_PERMISSIONS = 1;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    public HomeFragment homeFragment;
    PersonFragment personFragment;
    TasksFragment tasksFragment;
    SettingFragment settingFragment;
    SharedPreferences sharedPrefs;

    public BottomNavigationView getBottomNavigationView() {
        return bottomNavigationView;
    }

    public BottomNavigationView bottomNavigationView;
    FirebaseAuth mauth = FirebaseAuth.getInstance();
    public FloatingActionButton button;
    private int mMenuId;

    ArrayList<Map<String, Object>> _points = new ArrayList<>();
    ImageView imageView;
    public CardView cardView;
    public ArrayList<Task> getTasks() {
        return tasks;
    }
    public void setTasks(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    FirebaseService fb = new FirebaseService();
    FusedLocationProviderClient fusedLocationClient;
    LinearLayout linearLayout;
    TextView tv_lat, tv_long, background_button_text;
    public TextView open_close;
    public ArrayList<Task> tasks;
    public boolean gps_enabled = false;
    public boolean network_enabled = false;
    Class<?> backgroundService  = ScanService.class;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        tasksFragment = new TasksFragment();
        homeFragment = new HomeFragment();
        personFragment = new PersonFragment();
        settingFragment = new SettingFragment();
        imageView = findViewById(R.id.image);
        button = findViewById(R.id.button);
        tv_lat = findViewById(R.id.tv_latitude);
        tv_long = findViewById(R.id.tv_longitude);
        linearLayout = findViewById(R.id.linearLayout_latlang);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        background_button_text = findViewById(R.id.background_button_text);
        open_close = findViewById(R.id.open_close);

        loadFragment(homeFragment);
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            new AlertDialog.Builder(this)
                    .setMessage("GPS Kapalı")
                    .setPositiveButton("Konum ayarları", (paramDialogInterface, paramInt) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .setNegativeButton("iptal",null)
                    .show();
        }
        changeView();
        cardView = findViewById(R.id.cardView);
        bottomNavigationView.setOnItemSelectedListener(this);
        int menuItemId = bottomNavigationView.getMenu().getItem(3).getItemId();


        button.setOnClickListener(v ->{
            if(PreferencesHelper.getInstance(this).readData(BACKGROUND_KEY)) {
                    startBackgroundService();
                    button.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.track_location));
                    open_close.setText(R.string.background_open);
            }else{
                Utils.showBackgroundDialog(this);
                changeView();
                }
            }
        );
    }
    @Override
    public void onBackPressed() {
        AlertDialog diaBox = AskOption();
        diaBox.show();
    }
    private AlertDialog AskOption()
    {
        AlertDialog myQuittingDialogBox =new AlertDialog.Builder(this)
                .setTitle(getString(R.string.exit_app))
                .setMessage(getString(R.string.exit_app_message))
                .setIcon(R.drawable.delete)
                .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> finishAffinity())
                .setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss())
                .create();
        return myQuittingDialogBox;
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, fragment)
                    .commit();
            return true;
        }
        Toast.makeText(this, getResources().getString(R.string.check_gps_internet), Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(PreferencesHelper.getInstance(this).readData(BACKGROUND_KEY)) {
            startBackgroundService();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(_points.size() > 0){
            String timeStamp = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss").format(Calendar.getInstance().getTime());
            fb.pushFiles(this,_points,currentUser.getUid(),timeStamp);
            _points.clear();
            System.out.println("locations sent.!");
        }
        System.out.println("destroyed.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("Resumed.");
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("Stopped.");
        if(PreferencesHelper.getInstance(this).readData(BACKGROUND_KEY)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (fusedLocationClient != null) {
                    fusedLocationClient.removeLocationUpdates(new LocationCallback() {
                        @Override
                        public void onLocationResult(@NonNull @NotNull LocationResult locationResult) {
                            System.out.println(locationResult.getLastLocation().getLatitude());
                            super.onLocationResult(locationResult);
                        }
                    });
                }
                startBackgroundService();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0 && grantResults.length > 0){
            for(int i=0; i<grantResults.length; i++){
        if(grantResults[i] == PackageManager.PERMISSION_GRANTED){
                 Toast.makeText(this, permissions[i] + " Granted.", Toast.LENGTH_SHORT).show();
        }
        else if (grantResults[i] == PackageManager.PERMISSION_DENIED  && Build.VERSION.SDK_INT> 23){
            new AlertDialog.Builder(this).setTitle(getString(R.string.need_permission_alert))
        .setMessage(getString(R.string.need_permission_content))
        .setPositiveButton(getResources().getString(R.string.ok), (dialog, which) -> {
            FirstLaunchActivity.goToSettings(this);
            finishAffinity();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION },requestCode);
            }
            dialog.dismiss();
        }).setNegativeButton(getString(R.string.block), (dialog, which) -> {
            dialog.dismiss();
            finishAffinity();
        }).create().show();}
        else{
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        } }
        }
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
        final int HOME = R.id.home;
        final int PERSON = R.id.person;
        final int SETTINGS = R.id.settings;
        final int TASKS = R.id.tasks;
       mMenuId = item.getItemId();
       for(int i=0; i<bottomNavigationView.getMenu().size(); i++){
           MenuItem menuItem = bottomNavigationView.getMenu().getItem(i);
           boolean isChecked = menuItem.getItemId() == item.getItemId();
       }
       switch (item.getItemId()){
           case HOME: {
               loadFragment(homeFragment);
           }break;
           case PERSON:{
               loadFragment(personFragment);
           }break;

           case SETTINGS:{
               loadFragment(settingFragment);
           }break;

           case TASKS:{
               if(tasks != null) {
                   loadFragment(tasksFragment);
               }
           }break;
       }
       return true;
    }


    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void startBackgroundService(){
        if(!isMyServiceRunning(backgroundService)) {
            if(FirebaseAuth.getInstance().getCurrentUser() != null){
                Intent i = new Intent(getApplicationContext(),backgroundService);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(this, i);
                }
                else {
                    startService(i);
                }
            }
            else{
                Toast.makeText(getApplicationContext(), "Kullanıcı Bulunamadı.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    void changeView(){
        if(!PreferencesHelper.getInstance(this).readData(BACKGROUND_KEY)){  button.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.location_track_red));}
        else {button.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.track_location));}
        loadFragment(homeFragment);
        if(PreferencesHelper.getInstance(this).readData(BACKGROUND_KEY)){
            if(!isMyServiceRunning(ScanService.class)){
                open_close.setText(R.string.ready);
            }else {
                open_close.setText(getString(R.string.background_open));
            }
        }else{
            open_close.setText(getString(R.string.background_closed));
        }

    }
}