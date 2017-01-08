package com.lukechenshui.beatpulse.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by luke on 12/10/16.
 */

public class Song extends RealmObject implements Parcelable, Comparable<Song> {

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
    @Ignore
    private final String TAG = "Song";
    String hash;
    String name;
    String title;
    String album;
    String artist;
    @PrimaryKey
    String fileLocation;
    String directory;

    public Song() {

    }

    public Song(File file) {
        name = file.getName();
        if(file.exists()){
            fileLocation = file.getAbsolutePath();
            File parent = file.getParentFile();
            if(parent != null){
                directory = parent.getAbsolutePath();
            }
            try {
                AudioFile audioFile = AudioFileIO.read(getFile());
                Tag tag = audioFile.getTag();
                title = tag.getFirst(FieldKey.TITLE);
                album = tag.getFirst(FieldKey.ALBUM);
                artist = tag.getFirst(FieldKey.ARTIST);
                if (title.equals("")) {
                    title = name;
                }
                if (album.equals("")) {
                    album = "Unknown Album";
                }
                if (artist.equals("")) {
                    artist = "Uknown Artist";
                }
                Log.d(TAG, "Title: " + title + " album: " + album + " artist: " + artist);
            } catch (Exception exc) {
                Log.d(TAG, "Exception occurred while loading metadata for " + file.getAbsolutePath(), exc);
            }

            //this.hash = Utility.getMD5OfFile(file);
        }
        else{
            Log.d("Song", "File passed to Song constructor doesn't exist for song name " + name);
        }

    }

    protected Song(Parcel in) {
        hash = in.readString();
        name = in.readString();
        fileLocation = in.readString();
        directory = in.readString();
        title = in.readString();
        album = in.readString();
        artist = in.readString();
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
        if (fileLocation != null) {
            return new File(fileLocation);
        } else {
            return null;
        }

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
        dest.writeString(directory);
        dest.writeString(title);
        dest.writeString(album);
        dest.writeString(artist);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Song){
            Song songObj = (Song)obj;
            return fileLocation.equals(songObj.fileLocation);
        }
        else{
            return false;
        }
    }

    @Override
    public int compareTo(Song song) {
        return name.compareTo(song.name);
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }
}