package com.lukechenshui.beatpulse.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.lukechenshui.beatpulse.Config;
import com.lukechenshui.beatpulse.R;
import com.lukechenshui.beatpulse.SharedData;
import com.lukechenshui.beatpulse.layout.PlayActivity;
import com.lukechenshui.beatpulse.models.Playlist;
import com.lukechenshui.beatpulse.models.Song;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;
import io.realm.RealmList;

import static android.app.Notification.PRIORITY_MAX;

public class MusicService extends Service {
    private final String TAG = "MusicService";
    MusicBinder binder = new MusicBinder();
    Song song;
    Playlist playlist;
    Long pausePos = null;



    SimpleExoPlayer player;

    boolean showNotification;
    ExecutorService executor = Executors.newFixedThreadPool(1);
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
        if(player.getPlaybackState() == ExoPlayer.STATE_READY){
            pausePos = player.getCurrentPosition();
            player.setPlayWhenReady(false);
        }
        if(showNotification){
            showNotification();
        }

    }

    public void play(){
        try{
            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(),
                    Util.getUserAgent(getApplicationContext(), "BeatPulse"));
// Produces Extractor instances for parsing the media data.
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
// This is the MediaSource representing the media to be played.
            MediaSource songSource = new ExtractorMediaSource(song.getFileUri(),
                    dataSourceFactory, extractorsFactory, null, null);
            player.prepare(songSource);

            player.setPlayWhenReady(true);

            if(pausePos != null){
                player.seekTo(pausePos);
            }

        }
        catch (Exception exc){
            //Catches the exception raised when preparing the same FFmpegMediaPlayer multiple times.
            Log.d(TAG, "Exception occurred while starting to play " + song.getName(), exc);
        }

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(song);
        realm.commitTransaction();
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

    public void init(final Song song, Playlist newPlaylist){

        Handler handler = new Handler();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory trackSelection = new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(handler, trackSelection);
        LoadControl loadControl = new DefaultLoadControl();

        resetPlayer();
        player = ExoPlayerFactory.newSimpleInstance(getApplicationContext(), trackSelector, loadControl);
        player.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if(playbackState == ExoPlayer.STATE_ENDED){
                    playNext();
                }
            }

            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity() {

            }
        });
        if(song != null){
            this.song = song;
            playlist = newPlaylist;
            loadPlaylist();
            playSong(song);
        }
    }

    public boolean isPaused() {
        if (player != null) {
            return !player.getPlayWhenReady();
        } else {
            return true;
        }
    }

    private void loadPlaylist(){
        playlist = new Playlist();
        File parentFile = song.getFile().getParentFile();
        String playlistName = parentFile != null ? parentFile.getName() : "Unknown Playlist";

        SharedData.init();
        RealmList<Song> songs = null;
        String origin = SharedData.getOrigin(getApplicationContext());
        if(origin != null){
            switch (origin){
                case "all_songs":
                    songs = SharedData.getAllSongs();
                    break;
                case "folder":
                    songs = SharedData.getSongsFromFolder(song);
            }
        }
        else{
            songs = SharedData.getAllSongs();
        }

        playlist.setSongs(songs);

        RealmList<Song> playListSongs = playlist.getSongs();

        if(playListSongs.contains(song)){
            playlist.setLastPlayedPosition(playListSongs.lastIndexOf(song));
        }

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(song);
        realm.copyToRealmOrUpdate(playlist);
        realm.commitTransaction();
    }

    private void resetPlayer(){
        if(player != null){
            pausePos = null;
            player.stop();
        }
    }

    public void playNext(){

        Song nextSong = null;
        if(playlist != null){
            int pos = playlist.getLastPlayedPosition();
            RealmList<Song> playlistSongs = playlist.getSongs();

            if(playlistSongs != null) {
                if (pos + 1 <= playlistSongs.size() - 1) {
                    pos++;
                    playlist.setLastPlayedPosition(pos);
                    nextSong = playlistSongs.get(pos);
                } else {
                    if (playlistSongs.size() > 0) {
                        nextSong = playlistSongs.first();
                        playlist.setLastPlayedPosition(0);
                    } else {
                        nextSong = song;
                    }
                }
            }
            else{
                nextSong = song;
            }
        }
        else{
            nextSong = song;
        }

        playSong(nextSong);
    }

    public void playPrevious(){
        Song nextSong = null;
        if(playlist != null){
            int pos = playlist.getLastPlayedPosition();
            RealmList<Song> playlistSongs = playlist.getSongs();
            if(playlistSongs != null){
                if(pos-1 >= 0){
                    pos--;
                    playlist.setLastPlayedPosition(pos);
                    nextSong = playlistSongs.get(pos);
                }
                else{
                    playlist.setLastPlayedPosition(playlistSongs.size()-1);
                    if(playlistSongs.size() > 0){
                        nextSong = playlistSongs.last();
                    }
                    else{
                        nextSong = song;
                    }

                }
            }
            else{
                nextSong = song;
            }
        }
        else{
            nextSong = song;
        }
        playSong(nextSong);
    }

    private void playSong(Song songToPlay){
        //loadPlaylist();
        resetPlayer();

        Config.setLastSong(songToPlay, getApplicationContext());
        if(isShowNotification()){
            showNotification();
        }
        song = songToPlay;
        play();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null && intent.getAction() != null) {
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

        Intent playIntent = new Intent(this, MusicService.class);
        int playButtonId;
        String playButtonString;

        if(player.getPlayWhenReady()){
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

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public SimpleExoPlayer getPlayer() {
        return player;
    }

    public void setPlayer(SimpleExoPlayer player) {
        this.player = player;
    }

    public Song getSong() {
        return song;
    }

    public boolean isShowNotification() {
        return true;
    }

    public void setShowNotification(boolean showNotification) {
        this.showNotification = true;
        showNotification = true;
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

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
}
