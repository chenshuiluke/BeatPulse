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
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.lukechenshui.beatpulse.Config;
import com.lukechenshui.beatpulse.R;
import com.lukechenshui.beatpulse.layout.PlayActivity;
import com.lukechenshui.beatpulse.models.Playlist;
import com.lukechenshui.beatpulse.models.Song;

import java.io.IOException;

public class MusicService extends Service {
    private final String TAG = "MusicService";
    MusicBinder binder = new MusicBinder();
    Song song;
    Playlist playlist;
    MediaPlayer player;
    boolean showNotification;
    public MusicService() {

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
                if(isShowNotification()){
                    showNotification();
                }

            }
            catch (IOException exc){
                Log.d(TAG, "An exception occurred while loading song " + song.getFileUri(), exc);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if(intent.getAction() != null){
            if (intent.getAction().equals(Config.ACTION.PREV_ACTION)) {
                Log.i(TAG, "Clicked Previous");

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
                        pnextIntent).build();
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
