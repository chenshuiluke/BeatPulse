package com.lukechenshui.beatpulse.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.lukechenshui.beatpulse.Config;
import com.lukechenshui.beatpulse.models.Playlist;
import com.lukechenshui.beatpulse.models.Song;

import java.io.IOException;

public class MusicService extends Service {
    private final String TAG = "MusicService";
    MusicBinder binder = new MusicBinder();
    Song song;
    Playlist playlist;
    MediaPlayer player;
    public MusicService() {

    }

    public void pause(){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("MEDIA_PLAYER_PAUSED");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);

        if(player.isPlaying()){
            player.pause();
        }

    }

    public void play(){
        try{
            player.prepare();
        }
        catch (Exception exc){
            //Catches the exception raised when preparing the same MediaPlayer multiple times.
        }
        player.start();
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("MEDIA_PLAYER_STARTED");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);
    }



    public void stop(){
        player.stop();
    }

    @Override
    public IBinder onBind(Intent intent) {
        //onBind is only called when the first client connects. All other bind attempts just return the same binder object
        return binder;
    }

    public void init(Song song){

        if(player != null){
            player.stop();
            player.release();
        }
        if(song != null){
            this.song = song;
            player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try{
                player.setDataSource(getApplicationContext(), song.getFileUri());
                Config.setLastSong(song, getApplicationContext());
            }
            catch (IOException exc){
                Log.d(TAG, "An exception occurred while loading song " + song.getFileUri(), exc);
            }
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public class MusicBinder extends Binder {
        public MusicService getService(){
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public MediaPlayer getPlayer() {
        return player;
    }

    public void setPlayer(MediaPlayer player) {
        this.player = player;
    }

    public Song getSong() {
        return song;
    }
}
