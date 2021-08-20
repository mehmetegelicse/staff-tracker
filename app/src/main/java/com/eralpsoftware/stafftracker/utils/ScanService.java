package com.eralpsoftware.stafftracker.utils;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.eralpsoftware.stafftracker.MainActivity;
import com.example.stafftracker.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ScanService extends Service {

    private LocationListener _locListener;
    private LocationManager _locManager;
    FusedLocationProviderClient fusedLocationProviderClient;
    ArrayList<Map<String, Object>> _points = new ArrayList<>();
    ArrayList<Location> hourlyPoints = new ArrayList<>();
    FirebaseService fb = new FirebaseService();
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private Handler mHandler;
    NotificationManager notificationManager;
    final String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";
    public static final String CHANNEL_ID = "ForegroundServiceChannel";



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

     //   PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);
        Toast.makeText(getApplicationContext(), "Servis Başlatıldı.", Toast.LENGTH_SHORT).show();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText("13123")
                .setSmallIcon(R.drawable.logo)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        mHandler = new Handler();

        mStatusChecker.run();
        movementChecker.run();
        pushNotification("Arkaplanda çalışıyor.");

        return START_STICKY;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (_points.size() >= 10) {
            String timeStamp = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss").format(Calendar.getInstance().getTime());

            fb.pushFiles(getApplicationContext(), _points, currentUser.getUid(), timeStamp);
            //     toneGen1.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 40);
            _points.clear();
        }
        stopForeground(true);
        mStatusChecker = null;
        movementChecker = null;
        pushNotification("Servis Durduruldu.");

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        System.out.println("onBind");

        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        System.out.println("OnRebind");
        super.onRebind(intent);
    }
    CancellationTokenSource cts = new CancellationTokenSource();
    Runnable mStatusChecker = new Runnable() {
        @SuppressLint("MissingPermission")
        @Override
        public void run() {

            try {
            } finally {
                fusedLocationProviderClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, cts.getToken()).addOnSuccessListener(location -> {
                    Map<String, Object> _point = new HashMap<>();

                    if (location != null) {
                        if (_point.isEmpty()) {
                            _point.put("latitude", location.getLatitude());
                            _point.put("longitude", location.getLongitude());
                            _point.put("time", System.currentTimeMillis());
                        } else {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                _point.replace("time", System.currentTimeMillis());
                                _point.replace("longitude", location.getLongitude());
                                _point.replace("latitude", location.getLatitude());

                            }
                            else{
                                _point.remove("time");
                                _point.remove("longitude");
                                _point.remove("latitude");
                                _point.put("latitude", location.getLatitude());
                                _point.put("longitude", location.getLongitude());
                                _point.put("time", System.currentTimeMillis());
                            }
                        }
                        _points.add(_point);
                     //   Toast.makeText(getApplicationContext(), _point.get("latitude") + "," + _point.get("longitude") + " \n" + _points.size(), Toast.LENGTH_SHORT).show();
                        //  Toast.makeText(getApplicationContext(), location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_SHORT).show();
                        if(_points.size()%10 == 0){
                            pushNotification("Konum verisi Alınıyor. \n" + new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss").format(_points.get(_points.size()-1).get("time")));

                        }
                        if (_points.size() >= 100) {
                            String timeStamp = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss").format(Calendar.getInstance().getTime());

                            fb.pushFiles(getApplicationContext(), _points, currentUser.getUid(), timeStamp);
                            //     toneGen1.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 40);
                            _points.clear();
                        }

                    }
                    else {
                        pushNotification("Konum alınamıyor. Lütfen ağ ayarlarınızı kontrol edin.");
                    }
                //    System.out.println(_point.get("latitude") + "," + _point.get("longitude") + "  ---> size :" + _points.size());


                });
                mHandler.postDelayed(mStatusChecker, 5000);
            }
        }

    };
    Runnable movementChecker = new Runnable() {
        double distance;
        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            try {
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                    hourlyPoints.add(location);
                    Toast.makeText(getApplicationContext(), "şimdiki location :" + location.getLatitude() + ", " + location.getLongitude(),
                            Toast.LENGTH_SHORT).show();

                    if(hourlyPoints.size()>= 2){
                        double distance = distFromMeters(hourlyPoints.get(hourlyPoints.size()-1).getLatitude(),
                                hourlyPoints.get(hourlyPoints.size()-1).getLongitude(),
                                hourlyPoints.get(hourlyPoints.size()-2).getLatitude(),
                                hourlyPoints.get(hourlyPoints.size()-2).getLongitude() );

                        if(distance<=100){
                            Toast.makeText(getApplicationContext(), "Son 1 saatlik hareketin: " + Math.ceil(distance) + " metre.", Toast.LENGTH_SHORT).show();
                            fb.movementHandler(false);
                        }
                        else fb.movementHandler(true);

                    }
                });
            }catch (Exception e){
                System.out.println(e);
            }
            mHandler.postDelayed(movementChecker, 1000*60*30);

        }

    };
    private Timer timer;
    private TimerTask timerTask;
    public void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @SuppressLint("MissingPermission")
            public void run() {

            }
        };

        timer.schedule(timerTask, 2, 5000); //
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        System.out.println("TaskRemoved");
        super.onTaskRemoved(rootIntent);
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
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
    void pushNotification( String message){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_MAX);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.logo)
                .setTicker("Hearty365")
                //.setPriority(Notification.PRIORITY_MAX)
                .setContentTitle("Sales Tracker")
                .setContentText(message)
                .setContentInfo("Info");

        notificationManager.notify(/*notification id*/1, notificationBuilder.build());
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}