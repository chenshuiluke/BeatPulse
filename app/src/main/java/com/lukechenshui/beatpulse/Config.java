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
    public static final int ALBUM_DRAWER_ITEM_POS = 0;
    public static final int ALL_SONGS_DRAWER_ITEM_POS = 1;
    public static final int BROWSE_DRAWER_ITEM_POS = 2;
    public static final int NOW_PLAYING_DRAWER_ITEM_POS = 3;
    private static String lastFolderLocation;
    private static Drawer activeDrawer;
    private static Song lastSong;

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
            lastSong = new Song(file);
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
        File lastSongFile = lastSong.getFile();
        if (lastSongFile != null) {
            editor.putString("lastSong", lastSongFile.getAbsolutePath());
        }

        editor.commit();
    }

    public interface ACTION {
        String MAIN_ACTION = "com.lukechenshui.beatpulse.services.musicservice.action.main";
        String INIT_ACTION = "com.lukechenshui.beatpulse.services.musicservice.action.init";
        String PREV_ACTION = "com.lukechenshui.beatpulse.services.musicservice.action.prev";
        String PLAY_ACTION = "com.lukechenshui.beatpulse.services.musicservice.action.play";
        String PAUSE_ACTION = "com.lukechenshui.beatpulse.services.musicservice.action.pause";
        String NEXT_ACTION = "com.lukechenshui.beatpulse.services.musicservice.action.next";
        String STARTFOREGROUND_ACTION = "com.lukechenshui.beatpulse.services.musicservice.action.startforeground";
        String STOPFOREGROUND_ACTION = "com.lukechenshui.beatpulse.services.musicservice.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        int MUSIC_SERVICE = 101;
    }
}
