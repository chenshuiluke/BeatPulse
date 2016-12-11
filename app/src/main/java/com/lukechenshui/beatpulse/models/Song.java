package com.lukechenshui.beatpulse.models;

import android.net.Uri;
import android.util.Log;

import com.lukechenshui.beatpulse.Utility;

import java.io.File;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by luke on 12/10/16.
 */

public class Song extends RealmObject {
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
}
