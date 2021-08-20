package com.eralpsoftware.stafftracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;

import com.example.stafftracker.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashScreen extends AppCompatActivity {
    private FirebaseAuth mAuth;
    String languageCode = "en";

    private final int SPLASH_DISPLAY_LENGTH = 1000;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {

        super.onCreate(icicle);
        setLocale();
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_splash_screen);
    //    startService(new Intent(getBaseContext(),ScanService.class));
        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                route(mAuth.getCurrentUser() != null);

            }

        }, SPLASH_DISPLAY_LENGTH);

    }
    void route(boolean auth){
        Intent intent;
        if(auth){
            intent = new Intent(SplashScreen.this,MainActivity.class);
        }
        else{
            intent = new Intent(SplashScreen.this,LoginActivity.class);
        }
        startActivity(intent);
        SplashScreen.this.finish();

    }
void setLocale(){
    String languageToLoad  = Locale.getDefault().getLanguage(); // your language
    Locale locale = new Locale(languageToLoad);
    Locale.setDefault(locale);
    Configuration config = new Configuration();
    config.locale = locale;
    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());}
}
