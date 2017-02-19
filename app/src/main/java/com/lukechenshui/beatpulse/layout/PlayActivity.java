package com.lukechenshui.beatpulse.layout;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayer;
import com.lukechenshui.beatpulse.Config;
import com.lukechenshui.beatpulse.DrawerInitializer;
import com.lukechenshui.beatpulse.R;
import com.lukechenshui.beatpulse.SharedData;
import com.lukechenshui.beatpulse.models.Playlist;
import com.lukechenshui.beatpulse.models.Song;
import com.lukechenshui.beatpulse.services.MusicService;
import com.mikepenz.materialdrawer.Drawer;
import com.triggertrap.seekarc.SeekArc;

import java.io.File;

import at.markushi.ui.CircleButton;
import io.realm.Realm;

public class PlayActivity extends ActionBarActivity {
    private final String TAG = "PlayActivity";
    private CircleButton previousSongButton;
    private CircleButton playOrPauseButton;
    private CircleButton nextSongButton;
    private CircleButton shuffleToggleButton;
    private CircleButton replayToggleButton;
    private MusicService musicService;
    private TextView marqueeTextView;
    private Song currentSong;
    private Playlist currentPlaylist;
    private SeekArc progressBar;
    private boolean bound;
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)iBinder;
            musicService = binder.getService();


            if (musicService != null) {
                musicService.init();
                if (SharedData.SongRequest.wasAccepted()) {
                    marqueeTextView.setText(musicService.getSong().getName());
                }
            }

            if (musicService.isPaused()) {
                playOrPauseButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            } else {
                playOrPauseButton.setImageResource(R.drawable.ic_pause_black_24dp);
            }

            if (musicService.isShuffling()) {
                shuffleToggleButton.setImageResource(R.drawable.ic_shuffle_white_24dp);
            } else {
                shuffleToggleButton.setImageResource(R.drawable.ic_arrow_forward_white_24dp);

            }

            if(musicService.isReplayingOneSong()){
                replayToggleButton.setImageResource(R.drawable.ic_repeat_one_white_24dp);
            }

            if(musicService.isReplayingAllSongs()){
                replayToggleButton.setImageResource(R.drawable.ic_repeat_white_24dp);
            }

            if (musicService.isStoppingAfterNextSong()) {
                replayToggleButton.setImageResource(R.drawable.ic_stop_white_24dp);
            }

            if (musicService.isPlayingToEndOfPlaylist()) {
                replayToggleButton.setImageResource(R.drawable.ic_play_to_end);
            }
            bound = true;
            Log.d(TAG, "Connected to music service");
            continuouslyUpdateProgressBar();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bound = false;
            Log.d(TAG, "Disconnected from music service");
        }
    };
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "MEDIA_PLAYER_PAUSED":
                    playOrPauseButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    marqueeTextView.setText(musicService.getSong().getName());
                    break;
                case "MEDIA_PLAYER_STARTED":
                    playOrPauseButton.setImageResource(R.drawable.ic_pause_black_24dp);
                    marqueeTextView.setText(musicService.getSong().getName());
                    break;
                case "PLAYBACK_MODE_SHUFFLE":
                    shuffleToggleButton.setImageResource(R.drawable.ic_shuffle_white_24dp);

                    Toast.makeText(context, "Shuffling enabled", Toast.LENGTH_SHORT).show();
                    break;

                case "PLAYBACK_MODE_NORMAL":
                    shuffleToggleButton.setImageResource(R.drawable.ic_arrow_forward_white_24dp);

                    Toast.makeText(context, "Shuffling disabled", Toast.LENGTH_SHORT).show();
                    break;

                case "REPLAY_MODE_ONE":
                    replayToggleButton.setImageResource(R.drawable.ic_repeat_one_white_24dp);

                    Toast.makeText(context, "Replaying one song", Toast.LENGTH_SHORT).show();
                    break;
                case "REPLAY_MODE_NONE":
                    replayToggleButton.setImageResource(R.drawable.ic_stop_white_24dp);

                    Toast.makeText(context, "Stopping after this song", Toast.LENGTH_SHORT).show();
                    break;
                case "REPLAY_MODE_ALL":
                    replayToggleButton.setImageResource(R.drawable.ic_repeat_white_24dp);

                    Toast.makeText(context, "Replaying all songs", Toast.LENGTH_SHORT).show();
                    break;
                case "PLAY_TO_END":
                    replayToggleButton.setImageResource(R.drawable.ic_play_to_end);

                    Toast.makeText(context, "Playing to the end of the list", Toast.LENGTH_SHORT).show();
                    break;
            }

            Log.d(TAG, "Received broadcast: " + intent.getAction());
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        if(musicService != null){
            musicService.setShowNotification(true);
        }
        if(bound){
            unbindService(connection);
            bound=false;
        }
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(musicService != null){
            musicService.setShowNotification(true);
        }
    }

    private void init(){
        progressBar = (SeekArc) findViewById(R.id.progressBar);
        progressBar.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int i, boolean fromUser) {
                if (fromUser) {
                    ExoPlayer player = musicService.getPlayer();
                    musicService.setPosition((long) (((float) i / (float) 100) * player.getDuration()));
                }

            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {
            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {
            }
        });
        Toolbar toolbar = (Toolbar) findViewById(R.id.thirdToolbar);
        setSupportActionBar(toolbar);
        Drawer drawer = DrawerInitializer.createDrawer(this, this, toolbar);

        drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        Config.setActiveDrawer(drawer);

        drawer.setSelection(Config.NOW_PLAYING_DRAWER_ITEM_POS + 1, false);
        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(getApplicationContext());

        Intent tempIntent = getIntent();
        if (tempIntent != null) {
            Uri uri = tempIntent.getData();
            if (uri != null) {
                SharedData.SongRequest.submitSongRequest(new Song(new File(uri.getPath())));
                SharedData.setNowPlayingOrigin(getApplicationContext(), "folder");
            }
            Log.d(TAG, "Intent data: " + uri);
        }

        if (SharedData.getNowPlayingOrigin(getApplicationContext()) != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("MEDIA_PLAYER_PAUSED");
            filter.addAction("MEDIA_PLAYER_STARTED");

            filter.addAction("PLAYBACK_MODE_SHUFFLE");
            filter.addAction("PLAYBACK_MODE_NORMAL");

            filter.addAction("REPLAY_MODE_NONE");
            filter.addAction("REPLAY_MODE_ALL");
            filter.addAction("REPLAY_MODE_ONE");
            filter.addAction("PLAY_TO_END");
            bManager.registerReceiver(receiver, filter);

            marqueeTextView = (TextView) findViewById(R.id.marqueeTextView);

            previousSongButton = (CircleButton) findViewById(R.id.previousSongButton);
            playOrPauseButton = (CircleButton) findViewById(R.id.playOrPauseButton);
            nextSongButton = (CircleButton) findViewById(R.id.nextSongButton);
            shuffleToggleButton = (CircleButton) findViewById(R.id.shuffleToggleButton);
            replayToggleButton = (CircleButton) findViewById(R.id.replayToggleButton);

            Intent intent = new Intent(this, MusicService.class);
            bindService(intent, connection, BIND_AUTO_CREATE);
            //Song temp = Config.getLastSong(getApplicationContext());
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Realm.init(getApplicationContext());
        setContentView(R.layout.activity_play);
    }

    public void startOrPauseMediaPlayer(View view){
        if(musicService != null){
            if(musicService.getPlayer().getPlayWhenReady()){
                musicService.pause();
            }
            else if(!musicService.getPlayer().getPlayWhenReady()){
                musicService.play();
            }
        }
    }

    public void playNextSong(View view){
        if(musicService != null){
            musicService.playNext(false, true);
        }
    }

    public void playPreviousSong(View view){
        if(musicService != null){
            musicService.playPrevious();
        }
    }

    public void toggleShuffle(View view) {
        if (musicService != null) {
            musicService.toggleShuffle();
        }
    }

    public void toggleReplay(View view){
        if(musicService != null){
            musicService.toggleReplay();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        //now getIntent() should always return the last received intent
    }

    public void continuouslyUpdateProgressBar() {
        Runnable updateProgressRunnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    ExoPlayer player = musicService.getPlayer();

                    final long percentage = (long) (((float) player.getCurrentPosition() / (float) player.getDuration()) * 100);

                    Log.d(TAG, "Player position: " + player.getCurrentPosition() + " duration: " + player.getDuration()
                            + " percentage: " + percentage);
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                progressBar.setProgress((int) percentage);
                            }
                        });

                        Thread.sleep(1000);
                    } catch (Exception exc) {
                        Log.d(TAG, "Exception occurred while updating progressbar value:", exc);
                    }
                }
            }
        };
        Thread thread = new Thread(updateProgressRunnable);
        thread.start();
    }
}
