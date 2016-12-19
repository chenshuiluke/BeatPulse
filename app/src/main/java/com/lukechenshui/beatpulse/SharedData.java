package com.lukechenshui.beatpulse;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.lukechenshui.beatpulse.models.Album;
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
    private static RealmResults<Album> albums;
    private static String origin;
    public static void init(){
        realm = Realm.getDefaultInstance();
        songs = realm.where(Song.class).findAll();
        playlists = realm.where(Playlist.class).findAll();
        albums = realm.where(Album.class).findAll();

        for(Song song : songs){
            Log.d(TAG, "Song loaded:" + song.getName());
        }

        for(Playlist playlist : playlists){
            Log.d(TAG, "Playlist loaded:" + playlist.getName() + " number of songs: " + playlist.getSongs().size());
        }

        for (Album album : albums) {
            Log.d(TAG, "Album loaded:" + album.getName() + " number of songs: " + album.getSongs().size());
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

    public static RealmList<Album> getAllAlbums() {
        RealmList<Album> list = new RealmList<>();
        if (albums != null) {
            list.addAll(albums);
        }

        return list;
    }

    public static RealmList<Song> getAlbumSongs(String name) {
        init();
        RealmList<Album> albums = getAllAlbums();
        RealmList<Song> songs = new RealmList<>();
        for (Album album : albums) {
            if (album.getName() != null && name != null) {
                if (album.getName().equals(name)) {
                    if (album.getSongs() != null) {
                        songs = album.getSongs();
                        break;
                    }
                }
            }
        }
        return songs;
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

    public static class SongRequest {
        static Song song = null;
        static boolean accepted = false;

        public static void submitSongRequest(Song songToRequest) {
            song = songToRequest;
            accepted = false;
        }

        public static Song acceptSongRequest() {
            accepted = true;
            return song;
        }

        public static boolean wasAccepted() {
            return accepted && !isRequestEmpty();
        }

        public static boolean isRequestEmpty() {
            return song == null;
        }
    }

    public static class PlaylistRequest {
        static Playlist playlist = null;
        static boolean accepted = false;

        public static void submitPlaylistRequest(Playlist playlistToRequest) {
            playlist = playlistToRequest;
            accepted = false;
        }

        public static Playlist acceptPlaylistRequest() {
            accepted = true;
            return playlist;
        }

        public static boolean wasAccepted() {
            return accepted;
        }
    }
}
