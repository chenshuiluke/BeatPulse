package com.lukechenshui.beatpulse;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

/**
 * Created by luke on 12/10/16.
 */

public class Config {
    private static String lastFolderLocation;

    public static String getLastFolderLocation(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        lastFolderLocation = sharedPref.getString("lastFolderLocation", Environment.getExternalStorageDirectory().getAbsolutePath());
        return lastFolderLocation;
    }

    public static void setLastFolderLocation(String lastFolderLocation, Context context) {
        Config.lastFolderLocation = lastFolderLocation;
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("lastFolderLocation", lastFolderLocation);
        editor.commit();
    }
}
