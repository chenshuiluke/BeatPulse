package com.lukechenshui.beatpulse.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import com.lukechenshui.beatpulse.models.Playlist;
import com.lukechenshui.beatpulse.models.Song;

import java.io.IOException;

public class MusicService extends Service {
    private final String TAG = "MusicService";
    Song song;
    Playlist playlist;
    MediaPlayer player;
    public MusicService() {

    }

    public void play(){
        try{
            player.prepare();
            player.start();
        }
        catch (IOException exc){
            Log.d(TAG, "An exception occurred while trying to play song " + song.getFileUri(), exc);
        }
    }

    public void stop(){
        player.stop();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        this.song = song;
        this.playlist = playlist;
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try{
            player.setDataSource(getApplicationContext(), song.getFileUri());
        }
        catch (IOException exc){
            Log.d(TAG, "An exception occurred while loading song " + song.getFileUri(), exc);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
