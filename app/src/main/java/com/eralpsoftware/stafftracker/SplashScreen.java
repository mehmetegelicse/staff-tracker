package com.eralpsoftware.stafftracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.eralpsoftware.stafftracker.utils.Utils;
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


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {

        super.onCreate(icicle);
        setLocale();
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_splash_screen);
        new CountDownTimer(1000, 1000) {
            public void onFinish() {
               route(mAuth.getCurrentUser() != null );
            }

            public void onTick(long millisUntilFinished) {
                // millisUntilFinished    The amount of time until finished.
            }
        }.start();

    }
    void route(boolean auth){
       // Intent i = new Intent(this, FirstLaunchActivity);
        if(auth){
            Utils.goToActivity(this, FirstLaunchActivity.class, false);

        }
        else{
            Utils.goToActivity(this, LoginActivity.class, true);
        }
    }
void setLocale(){
    String languageToLoad  = Locale.getDefault().getLanguage(); // your language
    Locale locale = new Locale(languageToLoad);
    Locale.setDefault(locale);
    Configuration config = new Configuration();
    config.locale = locale;
    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());}
}
