package com.lukechenshui.beatpulse.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.lukechenshui.beatpulse.Utility;


import java.io.File;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by luke on 12/10/16.
 */

public class Song extends RealmObject implements Parcelable {
    @PrimaryKey
    String hash;
    String name;
    String fileLocation;

    public Song() {

    }

    public Song(String name, File file) {
        this.name = name;
        if(file.exists()){
            fileLocation = file.getAbsolutePath();
            this.hash = Utility.getMD5OfFile(file);
        }
        else{
            Log.d("Song", "File passed to Song constructor doesn't exist for song name " + name);
        }

    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Uri getFileUri() {
        return Uri.parse(new File(fileLocation).toURI().toString());
    }

    public File getFile(){
        return new File(fileLocation);
    }

    protected Song(Parcel in) {
        hash = in.readString();
        name = in.readString();
        fileLocation = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(hash);
        dest.writeString(name);
        dest.writeString(fileLocation);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Song){
            Song songObj = (Song)obj;
            return hash.equals(songObj.hash);
        }
        else{
            return false;
        }
    }
}