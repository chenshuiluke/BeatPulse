package com.lukechenshui.beatpulse.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by luke on 12/10/16.
 */
public class Playlist extends RealmObject implements Parcelable {
    private RealmList<Song> songs;
    private String name;
    private int lastPlayedPosition = 0;




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

    public int getLastPlayedPosition() {
        return lastPlayedPosition;
    }

    public void setLastPlayedPosition(int lastPlayedPosition) {
        this.lastPlayedPosition = lastPlayedPosition;
    }

    public RealmList<Song> getSongs() {
        return songs;
    }

    public void setSongs(RealmList<Song> songs) {
        this.songs = songs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    protected Playlist(Parcel in) {
        songs = (RealmList) in.readValue(RealmList.class.getClassLoader());
        name = in.readString();
        lastPlayedPosition = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(songs);
        dest.writeString(name);
        dest.writeInt(lastPlayedPosition);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Playlist> CREATOR = new Parcelable.Creator<Playlist>() {
        @Override
        public Playlist createFromParcel(Parcel in) {
            return new Playlist(in);
        }

        @Override
        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };
}