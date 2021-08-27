package com.eralpsoftware.stafftracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesHelper {
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME= "params";

    private  static PreferencesHelper instance;
    private Context context;

    public static synchronized PreferencesHelper getInstance(Context applicationContext){

        if(instance == null){
            instance = new PreferencesHelper(applicationContext);
        }
        return instance;

    }

    public PreferencesHelper(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    public void writeData(String key, Boolean value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
    public Boolean readData(String key){
        return  sharedPreferences.getBoolean(key,false);
    }

    public void clearCache(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().commit();

    }
}
