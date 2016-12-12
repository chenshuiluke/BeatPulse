package com.lukechenshui.beatpulse.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.lukechenshui.beatpulse.Config;
import com.lukechenshui.beatpulse.R;
import com.lukechenshui.beatpulse.Utility;
import com.lukechenshui.beatpulse.layout.PlayActivity;
import com.lukechenshui.beatpulse.models.Playlist;
import com.lukechenshui.beatpulse.models.Song;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;

import static android.app.Notification.PRIORITY_MAX;

public class MusicService extends Service {
    private final String TAG = "MusicService";
    MusicBinder binder = new MusicBinder();
    Song song;
    Playlist playlist;
    MediaPlayer player;
    boolean showNotification;
    public MusicService() {

    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public void pause(){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("MEDIA_PLAYER_PAUSED");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);

        if(player.isPlaying()){
            player.pause();
        }
        if(showNotification){
            showNotification();
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
        if(showNotification){
            showNotification();
        }
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("MEDIA_PLAYER_STARTED");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);
    }



    public void stop(){
        player.stop();
        setShowNotification(false);
    }

    @Override
    public IBinder onBind(Intent intent) {
        //onBind is only called when the first client connects. All other bind attempts just return the same binder object
        return binder;
    }

    public void init(Song song, Playlist playlist){

        if(player != null){
            player.stop();
            player.release();
        }
        if(song != null){
            this.song = song;
            this.playlist = playlist;

            if(this.playlist == null){
                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                ArrayList<File> songsInSameDirectory = Utility.getListOfAudioFilesInDirectory(getApplicationContext());

                ArrayList<Song> songs = new ArrayList<Song>();

                for(File currSong : songsInSameDirectory){
                    Song newSong = new Song(currSong.getName(), currSong);
                    realm.copyToRealmOrUpdate(newSong);
                    songs.add(newSong);
                }
                File parentFile = song.getFile().getParentFile();
                String playlistName = parentFile != null ? parentFile.getName() : "Unknown Playlist";
                this.playlist = new Playlist(songs, playlistName);
                realm.copyToRealmOrUpdate(song);
                realm.copyToRealmOrUpdate(this.playlist);
                realm.commitTransaction();
            }

            if(this.playlist != null){
                RealmList<Song> playListSongs = this.playlist.getSongs();
                if(playListSongs.contains(song)){
                    this.playlist.setLastPlayedPosition(playListSongs.lastIndexOf(song));
                }
            }


            playSong(song);
        }
    }

    private void resetPlayer(){
        if(player != null){
            player.stop();
            player.reset();
        }
    }

    public void playNext(){
        int pos = playlist.getLastPlayedPosition();
        RealmList<Song> playlistSongs = playlist.getSongs();
        Song nextSong;
        if(pos+1 <= playlistSongs.size()){
            pos++;
            playlist.setLastPlayedPosition(pos);
            nextSong = playlistSongs.get(pos);
        }
        else{
            nextSong = playlistSongs.first();
        }
        playSong(nextSong);
    }

    public void playPrevious(){
        int pos = playlist.getLastPlayedPosition();
        RealmList<Song> playlistSongs = playlist.getSongs();
        Song nextSong;
        if(pos-1 >= 0){
            pos--;
            playlist.setLastPlayedPosition(pos);
            nextSong = playlistSongs.get(pos);
        }
        else{
            nextSong = playlistSongs.last();
        }
    }

    private void playSong(Song songToPlay){
        try{
            resetPlayer();
            if(player == null){
                player = new MediaPlayer();
            }
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    playNext();
                }
            });
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);

            player.setDataSource(getApplicationContext(), songToPlay.getFileUri());
            Config.setLastSong(songToPlay, getApplicationContext());
            if(isShowNotification()){
                showNotification();
            }
            song = songToPlay;
            play();
        }
        catch (IOException|IllegalStateException exc){
            Log.d(TAG, "An exception occurred while loading song " + songToPlay.getFileUri(), exc);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if(intent.getAction() != null){
            if (intent.getAction().equals(Config.ACTION.PREV_ACTION)) {
                Log.i(TAG, "Clicked Previous");
                playPrevious();
                Toast.makeText(this, "Clicked Previous!", Toast.LENGTH_SHORT)
                        .show();
            } else if (intent.getAction().equals(Config.ACTION.PLAY_ACTION)) {
                Log.i(TAG, "Clicked Play");
                play();
                Toast.makeText(this, "Clicked Play!", Toast.LENGTH_SHORT).show();
            } else if (intent.getAction().equals(Config.ACTION.PAUSE_ACTION)) {
                Log.i(TAG, "Clicked Pause");
                pause();
                Toast.makeText(this, "Clicked Pause!", Toast.LENGTH_SHORT).show();
            }
            else if (intent.getAction().equals(Config.ACTION.NEXT_ACTION)) {
                Log.i(TAG, "Clicked Next");
                playNext();
                Toast.makeText(this, "Clicked Next!", Toast.LENGTH_SHORT).show();
            } else if (intent.getAction().equals(
                    Config.ACTION.STOPFOREGROUND_ACTION)) {
                Log.i(TAG, "Received Stop Foreground Intent");
                stopForeground(true);
                stopSelf();
            }
        }

        return START_STICKY;
    }

    private void showNotification() {
        //Call this again to update an existing notification as well.
        Intent notificationIntent = new Intent(this, PlayActivity.class);
        notificationIntent.setAction(Config.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent previousIntent = new Intent(this, MusicService.class);
        previousIntent.setAction(Config.ACTION.PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);



        Intent nextIntent = new Intent(this, MusicService.class);
        nextIntent.setAction(Config.ACTION.NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_music_note_black_24dp);

        Intent playIntent = new Intent(this, MusicService.class);;
        int playButtonId;
        String playButtonString;

        if(player.isPlaying()){
            playButtonId = R.drawable.ic_pause_white_24dp;
            playButtonString = "Pause";
            playIntent.setAction(Config.ACTION.PAUSE_ACTION);
        }
        else{
            playButtonId = R.drawable.ic_play_arrow_white_24dp;
            playButtonString = "Play";
            playIntent.setAction(Config.ACTION.PLAY_ACTION);
        }
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("BeatPulse")
                .setTicker("BeatPulse")
                .setContentText(song.getName())
                .setSmallIcon(R.drawable.ic_music_note_black_24dp)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_previous, "Previous",
                        ppreviousIntent)
                .addAction(playButtonId, playButtonString,
                        pplayIntent)
                .addAction(android.R.drawable.ic_media_next, "Next",
                        pnextIntent)
                .setPriority(PRIORITY_MAX)
                .setWhen(0)
                .build();
        startForeground(Config.NOTIFICATION_ID.MUSIC_SERVICE,
                notification);

    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);

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

    public boolean isShowNotification() {
        return showNotification;
    }

    public void setShowNotification(boolean showNotification) {
        this.showNotification = showNotification;
        if(showNotification){
            showNotification();
        }
        else{
            String notificationService = Context.NOTIFICATION_SERVICE;
            NotificationManager manager = (NotificationManager)getApplicationContext().getSystemService(notificationService);
            stopForeground(true);
            manager.cancelAll();
        }

    }
}
