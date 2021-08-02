package com.example.stafftracker;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.provider.Settings;

import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stafftracker.utils.BackgroundService;
import com.example.stafftracker.utils.FirebaseService;
import com.example.stafftracker.view.HomeFragment;
import com.example.stafftracker.view.PersonFragment;
import com.example.stafftracker.view.TasksFragment;
import com.google.android.gms.location.FusedLocationProviderClient;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.stafftracker.utils.PermissionUtils.statusCheck;
import static com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;


public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    final private int REQUEST_CODE_ASK_PERMISSIONS = 1;
    HomeFragment homeFragment = new HomeFragment();
    PersonFragment personFragment = new PersonFragment();
    TasksFragment tasksFragment = new TasksFragment();
    BottomNavigationView bottomNavigationView;
    FirebaseAuth mauth = FirebaseAuth.getInstance();
    public FloatingActionButton button;
    private int mMenuId;
    ArrayList<Map<String, Object>> _points = new ArrayList<>();
    ImageView imageView;

    FirebaseService fb = new FirebaseService();
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    FusedLocationProviderClient fusedLocationClient;
    final int LOCATION_GETTING_PERIOD = 5000;  // 5 seconds
    final int MAX_ARRAY_SIZE = 200;
    private Handler mHandler;
    boolean onLoop =  false;
    LinearLayout linearLayout;
    SwitchMaterial switchMaterial;
    TextView tv_lat, tv_long;
    ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_SYSTEM, 100);




    private boolean checkCoarseLocation(){
        return ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    private boolean checkBackgroundLocation(){
        return ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void requestPermission(){
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        if(!checkCoarseLocation()){
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if(!checkBackgroundLocation()){
            permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }
        if(!permissionsToRequest.isEmpty()){
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[permissionsToRequest.size()]),0);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        imageView = findViewById(R.id.image);
        button = findViewById(R.id.button);
        tv_lat = findViewById(R.id.tv_latitude);
        tv_long = findViewById(R.id.tv_longitude);
        linearLayout = findViewById(R.id.linearLayout_latlang);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        checkUserPermission();
       // requestPermission();

        bottomNavigationView.setOnItemSelectedListener(this);
        button.setBackgroundColor(Color.GREEN);
        button.setOnClickListener(v ->{
            System.out.println(onLoop);

                    if(!onLoop){
                        button.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.location_track_red));
                        mHandler = new Handler();
                        startRepeatingTask();
                        onLoop  = !onLoop;
                    }
                    else stopRepeatingTask();
                }
        );
        switchMaterial = findViewById(R.id.switch_widget);
        switchMaterial.setOnClickListener(v -> {
            System.out.println(switchMaterial.isChecked());
            new AlertDialog.Builder(this)
                    .setTitle("Arkaplan Takibi")
                    .setMessage("Etkinleştirildeğinde uygulama kapatıldığı anda konum verisine erişilecek.")
                    .setNeutralButton("Tamam",null)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();


        });
        statusCheck(this);

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
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, fragment)
                    .commit();
            return true;
        }
        return false;
    }



    public boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION }, 1);
            }
        } else return true;
        return false;
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("background killed!");
        if(_points.size() > 0){
            String timeStamp = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss").format(Calendar.getInstance().getTime());
            fb.pushFiles(this,_points,currentUser.getUid(),timeStamp);
            _points.clear();
            System.out.println("locations sent.!");
        }
        System.out.println("destroyed.");
        //
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("Resumed.");

        stopService(new Intent(this, BackgroundService.class));
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("Stopped.");
        if( ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED && switchMaterial.isChecked()) {
            if (fusedLocationClient != null) {
                fusedLocationClient.removeLocationUpdates(new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull @NotNull LocationResult locationResult) {
                        System.out.println(locationResult.getLastLocation().getLatitude());
                        super.onLocationResult(locationResult);
                    }
                });
            }
            startService(new Intent(this, BackgroundService.class));
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
        else if (grantResults[i] == PackageManager.PERMISSION_DENIED){
            new AlertDialog.Builder(this).setTitle(getString(R.string.need_permission_alert))
        .setMessage(getString(R.string.need_permission_content))
        .setPositiveButton("TAMAM", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},requestCode);
                dialog.dismiss();
            }
        }).setNegativeButton(getString(R.string.block), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                finishAffinity();

            }
        }).create().show();}
        else{
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }

            }
        }
    }
    private void checkUserPermission() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                //check if all permission are granted
                if (report.areAllPermissionsGranted()) {
                    loadFragment(
                            new HomeFragment()
                    );
                    System.out.println("granted");
                } else {
                    List<PermissionDeniedResponse> responses = report.getDeniedPermissionResponses();
                    StringBuilder permissionsDenied = new StringBuilder("Permissions denied: ");
                    for (PermissionDeniedResponse response : responses) {
                        permissionsDenied.append(response.getPermissionName()).append(" ") ;
                    }
                    Toast.makeText(MainActivity.this, permissionsDenied.toString(),Toast.LENGTH_SHORT).show();
                }

                if (report.isAnyPermissionPermanentlyDenied()) {
                    //permission is permanently denied navigate to user setting
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this)
                            .setTitle(getString(R.string.need_permission_alert))
                            .setMessage(getString(R.string.need_permission_blocked))
                            .setPositiveButton(getString(R.string.go_to_settings), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivityForResult(intent, 101);
                                }
                            })
                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                    dialog.show();

                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        })
                .onSameThread()
                .check();

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

           }break;

           case TASKS:{
               loadFragment(tasksFragment);
           }break;
       }
       return true;
    }
    Runnable mStatusChecker = new Runnable() {


        @SuppressLint("MissingPermission")
        @Override
        public void run() {


            try {
            } finally {
                statusCheck(MainActivity.this);
                fusedLocationClient.getCurrentLocation(PRIORITY_BALANCED_POWER_ACCURACY, new CancellationToken() {
                    @Override
                    public boolean isCancellationRequested() {
                        return false;
                    }

                    @NonNull
                    @NotNull
                    @Override
                    public CancellationToken onCanceledRequested(@NonNull @NotNull OnTokenCanceledListener onTokenCanceledListener) {
                        return null;
                    }
                }).addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location1) {

                        if (location1 != null) {
                            tv_lat.setText("Lat:   "+ new DecimalFormat("##.#######").format(location1.getLatitude()));
                            tv_long.setText("Lng:  " + new DecimalFormat("##.#######").format(location1.getLongitude()));
                            Map<String, Object> _point = new HashMap<>();
                            long a = 0;
                            System.out.println("location :" + location1.getLongitude());
                            if(!_points.isEmpty()){
                               a = (long) _points.get(_points.size()-1).get("time");
                            }
                            _point.put("latitude", location1.getLatitude());
                            _point.put("longitude", location1.getLongitude());
                           long b =  System.currentTimeMillis();
                            _point.put("time", b);
                            _points.add(_point);
                            System.out.println(_point.get("latitude") + ","  + _point.get("longitude") + "interval:" +(b-a)/1000 + "size :" + _points.size() );
                        }
                        if(_points.size() > MAX_ARRAY_SIZE){
                            String timeStamp = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss").format(Calendar.getInstance().getTime());
                            fb.pushFiles(getApplicationContext(), _points,currentUser.getUid(),timeStamp);
                            _points.clear();
                            toneGen1.startTone(ToneGenerator.TONE_SUP_RADIO_ACK,150);
                        }
                    }
                });
                mHandler.postDelayed(mStatusChecker, LOCATION_GETTING_PERIOD);
            }
        }

    };

    void startRepeatingTask() {
        animate(true);
        mStatusChecker.run();

    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
        onLoop  = !onLoop;
        animate(false);
        button.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.track_location));
        System.out.println("array_size :  " +_points.size());
        if(_points.size()>MAX_ARRAY_SIZE/10){
            String timeStamp = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss").format(Calendar.getInstance().getTime());
            fb.pushFiles(this,_points,currentUser.getUid(),timeStamp);
        }

    }

    void animate(boolean track){

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(imageView, "alpha",  1f, .01f);
        fadeOut.setDuration(1500);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(imageView, "alpha", .01f, 1f);
        fadeIn.setDuration(1500);

        final AnimatorSet mAnimationSet = new AnimatorSet();
        if(track) {
            imageView.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.VISIBLE);
            mAnimationSet.play(fadeIn).after(fadeOut);

            mAnimationSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mAnimationSet.start();
                }
            });
            mAnimationSet.start();
        }
        else{
            mAnimationSet.end();
            imageView.setVisibility(View.INVISIBLE);
            linearLayout.setVisibility(View.INVISIBLE);
        }
    }


}