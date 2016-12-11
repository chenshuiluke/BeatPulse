package com.lukechenshui.beatpulse;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import com.mikepenz.materialdrawer.Drawer;

/**
 * Created by luke on 12/10/16.
 */

public class Config {
    public static final int HOME_DRAWER_ITEM_POS = 0;
    public static final int BROWSE_DRAWER_ITEM_POS = 1;
    public static final int NOW_PLAYING_DRAWER_ITEM_POS = 2;
    private static String lastFolderLocation;
    private static Drawer activeDrawer;

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

    public static Drawer getActiveDrawer() {
        return activeDrawer;
    }

    public static void setActiveDrawer(Drawer activeDrawer) {
        Config.activeDrawer = activeDrawer;
    }
}
