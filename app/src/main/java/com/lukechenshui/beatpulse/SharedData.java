package com.lukechenshui.beatpulse;

import android.util.Log;

import com.lukechenshui.beatpulse.models.Playlist;
import com.lukechenshui.beatpulse.models.Song;

import java.io.File;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by luke on 12/13/16.
 */

public class SharedData {
    private static final String TAG = "SharedData";
    private static Realm realm;
    private static RealmResults<Song> songs;
    private static RealmResults<Playlist> playlists;
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
}
