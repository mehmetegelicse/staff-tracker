package com.eralpsoftware.stafftracker.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

/**
 * Created by Ayush Jain on 8/31/17.
 */


import com.example.stafftracker.R;

import java.text.DateFormat;
import java.util.Date;


public class Utils {
    public static final String BACKGROUND_KEY = "background";
    public static final String FIRST_LAUNCH_KEY = "first_launch_key";
    static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates";

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

    /**
     * Returns the {@code location} object as a human readable string.
     * @param location  The {@link Location}.
     */
    static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    static String getLocationTitle(Context context) {
        return context.getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));
    }
    public static void showBackgroundDialog(Context context) {
        if (!PreferencesHelper.getInstance(context).readData(BACKGROUND_KEY)) {
            new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.switch_text))
                    .setMessage(context.getString(R.string.background_warning))
                    .setNegativeButton(context.getString(R.string.exit_app), (dialog, which) -> PreferencesHelper.getInstance(context).writeData(BACKGROUND_KEY, false))
                    .setPositiveButton(context.getString(R.string.ok), (dialog, which) -> PreferencesHelper.getInstance(context).writeData(BACKGROUND_KEY, true))
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();
        }
    }
    public static void goToActivity(Activity from, Class<?> to, boolean setFlag){
        Intent intent = new Intent(from, to);
        if(setFlag){
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        from.startActivity(intent);


    }
}
