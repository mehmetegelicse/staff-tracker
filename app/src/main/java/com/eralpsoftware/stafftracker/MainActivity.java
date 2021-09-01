package com.eralpsoftware.stafftracker;

import static com.eralpsoftware.stafftracker.utils.Utils.BACKGROUND_KEY;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.eralpsoftware.stafftracker.utils.FirebaseService;
import com.eralpsoftware.stafftracker.utils.PreferencesHelper;
import com.eralpsoftware.stafftracker.utils.Utils;
import com.eralpsoftware.stafftracker.view.BottomSheetView;
import com.eralpsoftware.stafftracker.view.HomeFragment;
import com.eralpsoftware.stafftracker.view.SettingFragment;
import com.eralpsoftware.stafftracker.viewmodel.TaskItemAdapter;
import com.example.stafftracker.R;
import com.eralpsoftware.stafftracker.model.Task;
import com.eralpsoftware.stafftracker.utils.ScanService;
import com.eralpsoftware.stafftracker.view.PersonFragment;
import com.eralpsoftware.stafftracker.view.TasksFragment;
import com.github.mikephil.charting.charts.LineChart;
import com.google.android.gms.location.FusedLocationProviderClient;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import at.grabner.circleprogress.CircleProgressView;

public class MainActivity extends AppCompatActivity implements TaskItemAdapter.ITaskLocation {
    final private int REQUEST_CODE_ASK_PERMISSIONS = 1;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    public HomeFragment homeFragment;
    PersonFragment personFragment;
    TasksFragment tasksFragment;
    SettingFragment settingFragment;
    public FrameLayout mapContainer;
    public LinearLayout mainContainer;
    BottomSheetView bottomSheetView;
    CircleProgressView circleProgressView;
    public FloatingActionButton button;
    ConstraintLayout fragmentContainer;
    ArrayList<Map<String, Object>> _points = new ArrayList<>();
    ImageView imageView;
    public ArrayList<Task> getTasks() {
        return tasks;
    }
    public void setTasks(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    FirebaseService fb = new FirebaseService();
    FusedLocationProviderClient fusedLocationClient;
    TextView  background_button_text, daily_tasks_tv, total_tasks_tv, done_tasks_tv, onHoldTasks_tv;
    public TextView open_close;
    public ArrayList<Task> tasks;
    public boolean gps_enabled = false;
    public boolean network_enabled = false;
    Class<?> backgroundService  = ScanService.class;
    TaskItemAdapter taskItemAdapter;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    FloatingActionButton floatingActionButton;
    Button taskPageButton;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ImageButton imageButton;
    LineChart chart;
    ProgressBar pbar;

    public ProgressBar getPbar() {
        return pbar;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                // User chose the "Settings" item, show the app settings UI...
                loadFragment(settingFragment);
                return true;

            case R.id.action_search:
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        tasks = fetchTasks();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        tasksFragment = new TasksFragment();
        homeFragment = new HomeFragment();
        floatingActionButton = findViewById(R.id.floating_button);
        personFragment = new PersonFragment();
        settingFragment = new SettingFragment();
        imageView = findViewById(R.id.image);
        button = findViewById(R.id.button);
        background_button_text = findViewById(R.id.background_button_text);
        open_close = findViewById(R.id.open_close);
        mainContainer = findViewById(R.id.main_container);
        mapContainer = findViewById(R.id.flFragment);
        circleProgressView = findViewById(R.id.circleView);
        daily_tasks_tv = findViewById(R.id.daily_task_number);
        taskPageButton = findViewById(R.id.go_to_tasks);
        imageButton = findViewById(R.id.close_map);
        pbar = findViewById(R.id.transitive_progressBar);
        fragmentContainer = findViewById(R.id.map_container);
        done_tasks_tv = findViewById(R.id.done_tasks);
        onHoldTasks_tv = findViewById(R.id.on_hold);
        total_tasks_tv = findViewById(R.id.total_tasks);
        //  loadFragment(homeFragment);
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


        floatingActionButton.setOnClickListener( v -> getlocation());
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
        imageButton.setOnClickListener(v->{
            setTitle(getString(R.string.app_name));
            mainContainer.setVisibility(View.VISIBLE);
            fragmentContainer.setVisibility(View.GONE);
        });

    }
    @Override
    public void onBackPressed() {
        AlertDialog diaBox = AskOption();
        diaBox.show();
    }

    private AlertDialog AskOption() {
        AlertDialog myQuittingDialogBox =new AlertDialog.Builder(this)
                .setTitle(getString(R.string.exit_app))
                .setMessage(getString(R.string.exit_app_message))
                .setIcon(R.drawable.delete)
                .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> finishAffinity())
                .setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss())
                .create();
        return myQuittingDialogBox;
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

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            mainContainer.setVisibility(View.GONE);
            fragmentContainer.setVisibility(View.VISIBLE);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, fragment)
                    .commit();
            return true;
        }
        Toast.makeText(this, getResources().getString(R.string.check_gps_internet), Toast.LENGTH_SHORT).show();
        return false;
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

    void loadAdapter(ArrayList<Task> taskArrayList){
            SnapHelper snapHelper = new PagerSnapHelper();
            taskItemAdapter = new TaskItemAdapter(this, taskArrayList, this);
            recyclerView = findViewById(R.id.task_recyclerview);
            mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(mLayoutManager);
            snapHelper.attachToRecyclerView(recyclerView);
            recyclerView.setAdapter(taskItemAdapter);
    }

    @Override
    public void showTaskLocation(String id, double latitude, double longitude, String title, int status, String description, long createdAt, String taskName) {

        loadFragment(homeFragment);

    }
    public ArrayList<Task> fetchTasks(){
        ArrayList<Task> taskArrayList = new ArrayList<>();
        AtomicLong daily_time = new AtomicLong();
        db.collection("tasks").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                if(queryDocumentSnapshots.getDocuments().get(i).getData().get("userId").equals(currentUser.getUid())){
                    Task task = queryDocumentSnapshots.getDocuments().get(i).toObject(Task.class);
                    System.out.println(queryDocumentSnapshots.getDocuments().get(i).getData().get("createdAt"));
                    taskArrayList.add(task);
                }

            }
            loadAdapter(taskArrayList);

        }).addOnFailureListener(
                e -> System.out.println(e)
        ).addOnCompleteListener(task -> {

            Date date = new Date(System.currentTimeMillis());
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            String dateString = formatter.format(date);
            try {
                Date a = formatter.parse(dateString);
                System.out.println(a.getTime());
                daily_time.set(a.getTime());


            } catch (ParseException e) {
                e.printStackTrace();
            }



            ArrayList<Task> list = taskArrayList;
            int dailyTasks = 0;
            int waitingTask = 0;
            int completedTask = 0;
            int cancelledTask = 0;
            for (int i = 0; i < list.size(); i++) {
                long d = list.get(i).getCreatedAt();
                System.out.println(d);
                if(list.get(i).getStatus() == 1){
                    completedTask++;
                }
                if(list.get(i).getStatus() == 0){
                    waitingTask++;
                }
                if(list.get(i).getStatus() == 2){
                    cancelledTask++;
                }
                if(list.get(i).getCreatedAt() > daily_time.get() ){
                    dailyTasks++;
                }
            }
            circleProgressView.setMaxValue(list.size());
            circleProgressView.setValue(completedTask);
            if(dailyTasks ==0) {
                daily_tasks_tv.setText("Günlük görev yok.");
                taskPageButton.setEnabled(false);
            }else{
                daily_tasks_tv.setText(dailyTasks + " adet görev.");
                taskPageButton.setOnClickListener(v -> loadFragment(tasksFragment));
            }
            onHoldTasks_tv.setText(waitingTask + " beklemede,");
            done_tasks_tv.setText(completedTask + " tamamlanmış,");
            total_tasks_tv.setText(waitingTask + completedTask + " toplam");
        });
        return taskArrayList;
    }

    @SuppressLint("MissingPermission")
    private void getlocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            System.out.println();
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        System.out.println();
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            bottomSheetView = new BottomSheetView(location);
                            bottomSheetView.show(getSupportFragmentManager(), "bottomsheet");

                        }
                    });

        }
    }


}