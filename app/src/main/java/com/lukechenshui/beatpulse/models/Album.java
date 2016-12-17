package com.lukechenshui.beatpulse.models;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by luke on 12/16/16.
 */

/*
TODO: Allow users to play songs from albums
 */
public class Album extends RealmObject implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Album> CREATOR = new Parcelable.Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };
    private RealmList<Song> songs = new RealmList<>();
    @PrimaryKey
    private String name;

    public Album() {
    }

    public Album(String name) {
        this.name = name;
    }

    protected Album(Parcel in) {
        songs = (RealmList) in.readValue(RealmList.class.getClassLoader());
        name = in.readString();
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

    public void addSong(Song song) {
        songs.add(song);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(songs);
        dest.writeString(name);
    }
}