package com.eralpsoftware.stafftracker;


import static com.eralpsoftware.stafftracker.utils.Utils.FIRST_LAUNCH_KEY;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.eralpsoftware.stafftracker.utils.PreferencesHelper;
import com.example.stafftracker.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

public class FirstLaunchActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Checking for first time launch - before calling setContentView()
        if (PreferencesHelper.getInstance(this).readData(FIRST_LAUNCH_KEY)) {
           launchHomeScreen();
           finish();
        }

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        setContentView(R.layout.activity_first_launch);

        viewPager =  findViewById(R.id.view_pager);
        dotsLayout = findViewById(R.id.layoutDots);
        btnNext =  findViewById(R.id.btn_next);


        // layouts of welcome sliders
        layouts = new int[]{
                R.layout.welcome_slide1,
                R.layout.welcome_slide2
        };

        // adding bottom dots
        addBottomDots(0);

        // making notification bar transparent
        changeStatusBarColor();

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
        btnNext.setOnClickListener(v -> {
            // checking for last page if true launch MainActivity
            int current = getItem(+1);
            if (current < layouts.length) {
                // move to next screen
                viewPager.setCurrentItem(current);
                btnNext.setText(R.string.ANLADIM);
            } else {
                checkUserPermission();
            }
        });
    }

    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&bull;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    private void launchHomeScreen() {
        startActivity(new Intent(FirstLaunchActivity.this, MainActivity.class));
        finish();
    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);

            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.length - 1) {
                // last page. make button text to GOT IT
                btnNext.setText(getString(R.string.ANLADIM));

            } else {
                // still pages are left
                btnNext.setText(getString(R.string.next));
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    // Making notification bar transparent

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
    private void checkUserPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
                        System.out.println("granted");
                        PreferencesHelper.getInstance(FirstLaunchActivity.this).writeData(FIRST_LAUNCH_KEY, true);
                        launchHomeScreen();
                    } else {
                        List<PermissionDeniedResponse> responses = report.getDeniedPermissionResponses();
                        StringBuilder permissionsDenied = new StringBuilder("Permissions denied: ");
                        for (PermissionDeniedResponse response : responses) {
                            permissionsDenied.append(response.getPermissionName()).append(" ") ;
                        }
                        Toast.makeText(FirstLaunchActivity.this, permissionsDenied.toString(),Toast.LENGTH_SHORT).show();
                    }

                    if (report.isAnyPermissionPermanentlyDenied()) {
                        //permission is permanently denied navigate to user setting
                        AlertDialog.Builder dialog = new AlertDialog.Builder(FirstLaunchActivity.this)
                                .setTitle(getString(R.string.need_permission_alert))
                                .setMessage(getString(R.string.need_permission_blocked))
                                .setPositiveButton(getString(R.string.go_to_settings), (dialogInterface, i) -> {

                                    goToSettings(getApplicationContext());
                                })
                                .setNegativeButton("CANCEL", (dialogInterface, i) ->{  finishAffinity(); dialogInterface.dismiss();});
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
        else if(Build.VERSION.SDK_INT == Build.VERSION_CODES.M){
            System.out.println();
            ArrayList<String> permissionsToRequest = new ArrayList<>();
            if(!checkCoarseLocation()){
                permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            if (checkDeniedLocation()){
                goToSettings(this);
            }
            if(!permissionsToRequest.isEmpty()){
                ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[permissionsToRequest.size()]),0);
            }
            if(permissionsToRequest.isEmpty()){
                PreferencesHelper.getInstance(FirstLaunchActivity.this).writeData(FIRST_LAUNCH_KEY, true);
                goMainActivity();
            }
        }
        else{
            Dexter.withContext(this)
                    .withPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            if (report.areAllPermissionsGranted()) {
                                PreferencesHelper.getInstance(FirstLaunchActivity.this).writeData(FIRST_LAUNCH_KEY, true);
                                goMainActivity();
                                System.out.println("granted");
                            } else {
                                List<PermissionDeniedResponse> responses = report.getDeniedPermissionResponses();
                                StringBuilder permissionsDenied = new StringBuilder("Permissions denied: ");
                                for (PermissionDeniedResponse response : responses) {
                                    permissionsDenied.append(response.getPermissionName()).append(" ") ;
                                }
                                Toast.makeText(FirstLaunchActivity.this, permissionsDenied.toString(),Toast.LENGTH_SHORT).show();
                            }

                            if (report.isAnyPermissionPermanentlyDenied()  &&   Build.VERSION.SDK_INT> 23) {
                                //permission is permanently denied navigate to user setting
                                AlertDialog.Builder dialog = new AlertDialog.Builder(FirstLaunchActivity.this)
                                        .setTitle(getString(R.string.need_permission_alert))
                                        .setMessage(getString(R.string.need_permission_blocked))
                                        .setPositiveButton(getString(R.string.go_to_settings), (dialogInterface, i) -> {
                                            goToSettings(FirstLaunchActivity.this);
                                        })
                                        .setNegativeButton("CANCEL", (dialogInterface, i) -> {dialogInterface.cancel();
                                            finishAffinity();});
                                dialog.show();

                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                            permissionToken.continuePermissionRequest();
                        }
                    });
        }

    }
    public static void goToSettings(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(uri);
        context.startActivity(intent);
    }
    void goMainActivity(){
        Intent intent = new Intent(FirstLaunchActivity.this, MainActivity.class);
        startActivity(intent);

    }
    private boolean checkCoarseLocation(){
        return ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    private boolean checkDeniedLocation(){
        return ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED;
    }
    private boolean checkBackgroundLocation(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

}