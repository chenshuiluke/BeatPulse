package com.lukechenshui.beatpulse;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.lukechenshui.beatpulse.models.Playlist;
import com.lukechenshui.beatpulse.models.Song;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * Created by luke on 12/13/16.
 */

public class SharedData {
    private static final String TAG = "SharedData";
    private static Realm realm;
    private static RealmResults<Song> songs;
    private static RealmResults<Playlist> playlists;
    private static String origin;
    public static void init(){
        realm = Realm.getDefaultInstance();
        songs = realm.where(Song.class).findAll();
        playlists = realm.where(Playlist.class).findAll();

        for(Song song : songs){
            Log.d(TAG, "Song loaded:" + song.getName());
        }

        for(Playlist playlist : playlists){
            Log.d(TAG, "Playlist loaded:" + playlist.getName());
        }
    }

    public static RealmList<Song> getSongsFromFolder(Song songToCheck){
        RealmList<Song> list = new RealmList<>();
        File parent = songToCheck.getFile().getParentFile();
        if(parent != null){
            RealmResults<Song> accompanyingSongs = songs.where().contains("fileLocation", songToCheck.getDirectory()).findAll();
            list.addAll(accompanyingSongs);
        }
        return list;
    }

    public static RealmList<Song> getAllSongs(){
        RealmList<Song> list = new RealmList<>();
        list.addAll(songs);
        return list;
    }

    public static String getOrigin(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        origin = sharedPref.getString("origin", null);
        return origin;
    }

    public static void setOrigin(Context context, String origin) {
        SharedData.origin = origin;
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("origin", origin);
        editor.commit();
    }
}
