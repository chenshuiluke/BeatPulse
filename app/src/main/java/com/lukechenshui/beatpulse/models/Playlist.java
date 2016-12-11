package com.lukechenshui.beatpulse.models;

import java.util.ArrayList;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by luke on 12/10/16.
 */

public class Playlist extends RealmObject {
    private RealmList<Song> songs;
    private String name;

    public Playlist() {
    }

    public Playlist(RealmList<Song> songs, String name) {
        this.songs = songs;
        this.name = name;
    }

    public Playlist(ArrayList<Song> songs, String name) {
        this.songs.clear();
        this.songs.addAll(songs);
        this.name = name;
    }
}
