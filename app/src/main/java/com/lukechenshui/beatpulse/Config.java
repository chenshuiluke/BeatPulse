package com.lukechenshui.beatpulse;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import com.lukechenshui.beatpulse.models.Song;
import com.mikepenz.materialdrawer.Drawer;

import java.io.File;

/**
 * Created by luke on 12/10/16.
 */

public class Config {
    public static final int HOME_DRAWER_ITEM_POS = 0;
    public static final int ALL_SONGS_DRAWER_ITEM_POS = 1;
    public static final int BROWSE_DRAWER_ITEM_POS = 2;
    public static final int NOW_PLAYING_DRAWER_ITEM_POS = 3;
    private static String lastFolderLocation;
    private static Drawer activeDrawer;
    private static Song lastSong;

    public interface ACTION {
        public static String MAIN_ACTION = "com.lukechenshui.beatpulse.services.musicservice.action.main";
        public static String INIT_ACTION = "com.lukechenshui.beatpulse.services.musicservice.action.init";
        public static String PREV_ACTION = "com.lukechenshui.beatpulse.services.musicservice.action.prev";
        public static String PLAY_ACTION = "com.lukechenshui.beatpulse.services.musicservice.action.play";
        public static String PAUSE_ACTION = "com.lukechenshui.beatpulse.services.musicservice.action.pause";
        public static String NEXT_ACTION = "com.lukechenshui.beatpulse.services.musicservice.action.next";
        public static String STARTFOREGROUND_ACTION = "com.lukechenshui.beatpulse.services.musicservice.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.lukechenshui.beatpulse.services.musicservice.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        public static int MUSIC_SERVICE = 101;
    }
    
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

    public static Song getLastSong(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        String lastSongLocation = sharedPref.getString("lastSong", null);
        if(lastSongLocation != null){
            File file = new File(lastSongLocation);
            lastSong = new Song(file.getName(), file);
            return lastSong;
        }
        else{
            return null;
        }

    }

    public static void setLastSong(Song lastSong, Context context) {
        Config.lastSong = lastSong;
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("lastSong", lastSong.getFile().getAbsolutePath());
        editor.commit();
    }
}
