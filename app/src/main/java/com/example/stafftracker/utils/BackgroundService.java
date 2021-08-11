package com.example.stafftracker.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.stafftracker.MainActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

public class BackgroundService extends Service {

    FusedLocationProviderClient fusedLocationClient;
    FirebaseService fb = new FirebaseService();
    final int LOCATION_GETTING_PERIOD = 8000;  // 8 seconds
    final int MAX_ARRAY_SIZE = 100;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private Handler mHandler;
    boolean onLoop = false;
    ArrayList<Location> hourlyPoints = new ArrayList<>();
    ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_SYSTEM, 100);


    ArrayList<Map<String, Object>> _points = new ArrayList<>();


    @Override
    public void onCreate() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        super.onCreate();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            startMyOwnForeground();
        } else
            startForeground(1, new Notification());
        if (!onLoop) {
            mHandler = new Handler();
            startRepeatingTask();
            onLoop = !onLoop;

        } else stopRepeatingTask();
    }


    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = null;
        NotificationManager mNotificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext().getApplicationContext(), "notify_001");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            chan.setLightColor(Color.BLUE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        }

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(chan);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            notification = notificationBuilder.setOngoing(true)
                    .setContentTitle("App is running in background")
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
        }

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("background killed!");
        if (_points.size() > 0) {
            String timeStamp = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss").format(Calendar.getInstance().getTime());
            fb.pushFiles(this, _points, currentUser.getUid(), timeStamp);
            _points.clear();
            System.out.println("locations sent.!");
        }
        stoptimertask();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "example.permanence");
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Open App please.")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
    }


    private Timer timer;
    private TimerTask timerTask;

    public void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @SuppressLint("MissingPermission")
            public void run() {

            }
        };

        timer.schedule(timerTask, 2, LOCATION_GETTING_PERIOD); //
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    CancellationTokenSource cts = new CancellationTokenSource();
    Runnable mStatusChecker = new Runnable() {
        @SuppressLint("MissingPermission")
        @Override
        public void run() {

            try {
            } finally {
                fusedLocationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, cts.getToken()).addOnSuccessListener(new OnSuccessListener<Location>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onSuccess(Location location) {
                        Map<String, Object> _point = new HashMap<>();

                        if (location != null) {
                            if (_point.isEmpty()) {
                                _point.put("latitude", location.getLatitude());
                                _point.put("longitude", location.getLongitude());
                                _point.put("time", System.currentTimeMillis());
                            } else {
                                _point.replace("latitude", location.getLatitude());
                                _point.replace("longitude", location.getLongitude());
                                _point.replace("time", System.currentTimeMillis());
                            }
                            _points.add(_point);
                            Toast.makeText(BackgroundService.this, _point.get("latitude") + "," + _point.get("longitude") + " \n" + _points.size(), Toast.LENGTH_SHORT).show();
                            //  Toast.makeText(getApplicationContext(), location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(BackgroundService.this, "Hata oluştu.", Toast.LENGTH_SHORT).show();
                        }
                        System.out.println(_point.get("latitude") + "," + _point.get("longitude") + "  ---> size :" + _points.size());

                        if (_points.size() > MAX_ARRAY_SIZE) {
                            String timeStamp = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss").format(Calendar.getInstance().getTime());

                            fb.pushFiles(getApplicationContext(), _points, currentUser.getUid(), timeStamp);
                            toneGen1.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 40);
                            _points.clear();
                        }
                    }
                });
                mHandler.postDelayed(mStatusChecker, LOCATION_GETTING_PERIOD);
            }
        }

    };

    Runnable movementChecker = new Runnable() {
        double distance;
        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            try {
                fusedLocationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, cts.getToken()).addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        hourlyPoints.add(location);
                        Toast.makeText(BackgroundService.this, "şimdiki location :" + location.getLatitude() + ", " + location.getLongitude(),
                                Toast.LENGTH_SHORT).show();

                        if(hourlyPoints.size()>= 2){
                            double distance = distFromMeters(hourlyPoints.get(hourlyPoints.size()-1).getLatitude(),
                                    hourlyPoints.get(hourlyPoints.size()-1).getLongitude(),
                                    hourlyPoints.get(hourlyPoints.size()-2).getLatitude(),
                                    hourlyPoints.get(hourlyPoints.size()-2).getLongitude() );

                            if(distance<=100){
                                Toast.makeText(BackgroundService.this, "Son 1 saatlik hareketin: " + Math.ceil(distance) + " metre.", Toast.LENGTH_SHORT).show();
                                fb.movementHandler(false);
                            }
                            else fb.movementHandler(true);

                        }
                    }
                });
            }catch (Exception e){
                System.out.println(e);
            }
            mHandler.postDelayed(movementChecker, 1000*60*30);

        }

    };
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("Service OnBind");
        return null;
    }
    public double distFromMeters(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        return dist;
    }

    void startRepeatingTask() {
        mStatusChecker.run();
        movementChecker.run();
    }

    void stopRepeatingTask() {
      //  mHandler.removeCallbacks(mStatusChecker);
    }
}