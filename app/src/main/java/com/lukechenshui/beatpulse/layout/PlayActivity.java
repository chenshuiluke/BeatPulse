package com.lukechenshui.beatpulse.layout;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlayer;
import com.lukechenshui.beatpulse.Config;
import com.lukechenshui.beatpulse.DrawerInitializer;
import com.lukechenshui.beatpulse.R;
import com.lukechenshui.beatpulse.models.Playlist;
import com.lukechenshui.beatpulse.models.Song;
import com.lukechenshui.beatpulse.services.MusicService;
import com.mikepenz.materialdrawer.Drawer;

import at.markushi.ui.CircleButton;

public class PlayActivity extends ActionBarActivity {
    private final String TAG = "PlayActivity";
    private CircleButton previousSongButton;
    private CircleButton playOrPauseButton;
    private CircleButton nextSongButton;
    private MusicService musicService;
    private TextView marqueeTextView;


    private Song currentSong;
    private Playlist currentPlaylist;

    private boolean bound;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("MEDIA_PLAYER_PAUSED")){
                playOrPauseButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                marqueeTextView.setText(musicService.getSong().getName());
            }
            else if(intent.getAction().equals("MEDIA_PLAYER_STARTED")){
                playOrPauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                marqueeTextView.setText(musicService.getSong().getName());
            }
            Log.d(TAG, "Received broadcast: " + intent.getAction());
        }
    };
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)iBinder;
            musicService = binder.getService();

            if(musicService.getSong() == null || !musicService.getSong().equals(currentSong)
                    || (!musicService.getPlaylist().equals(currentPlaylist) && currentPlaylist != null)){
                //Doesn't restart the current song if the new song is the same as the currently playing one.
                musicService.init(currentSong, currentPlaylist);

                Animation marquee = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.marquee);
                marqueeTextView.startAnimation(marquee);
            }
            else{
                currentPlaylist = musicService.getPlaylist();
                currentSong = musicService.getSong();
                playOrPauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                marqueeTextView.setText(musicService.getSong().getName());
                Animation marquee = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.marquee);
                marqueeTextView.startAnimation(marquee);
            }
            try{
                //visualizerView.link(musicService.getPlayer());
            }
            catch (RuntimeException exc){
                Log.d(TAG, "Exception when starting visualization", exc);
            }

            musicService.setShowNotification(false);
            addBarGraphRenderers();
            addCircleBarRenderer();

            marqueeTextView.setText(currentSong.getName());
            Animation marquee = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.marquee);
            marqueeTextView.startAnimation(marquee);
            bound = true;
            Log.d(TAG, "Connected to music service");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bound = false;
            Log.d(TAG, "Disconnected from music service");
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

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(musicService != null){
            musicService.setShowNotification(true);
        }

    }

    private void init(){
        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(getApplicationContext());

        IntentFilter filter = new IntentFilter();
        filter.addAction("MEDIA_PLAYER_PAUSED");
        filter.addAction("MEDIA_PLAYER_STARTED");

        bManager.registerReceiver(receiver, filter);

        marqueeTextView = (TextView) findViewById(R.id.marqueeTextView);

        previousSongButton = (CircleButton) findViewById(R.id.previousSongButton);
        playOrPauseButton = (CircleButton) findViewById(R.id.playOrPauseButton);
        nextSongButton = (CircleButton) findViewById(R.id.nextSongButton);

        Toolbar toolbar = (Toolbar) findViewById(R.id.thirdToolbar);
        setSupportActionBar(toolbar);
        Drawer drawer = DrawerInitializer.createDrawer(this, this, toolbar);

        drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        Config.setActiveDrawer(drawer);

        drawer.setSelection(Config.NOW_PLAYING_DRAWER_ITEM_POS+1, false);

        Song song = getIntent().getParcelableExtra("song");
        currentPlaylist = getIntent().getParcelableExtra("playlist");
        currentSong = song;
        if(song != null){
            Intent intent = new Intent(this, MusicService.class);
            bindService(intent, connection, BIND_AUTO_CREATE);
        }
        else{
            Song temp = Config.getLastSong(getApplicationContext());
            if(temp != null){
                currentSong = temp;
                Intent intent = new Intent(this, MusicService.class);
                bindService(intent, connection, BIND_AUTO_CREATE);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            musicService.playNext();
        }
    }

    public void playPreviousSong(View view){
        if(musicService != null){
            musicService.playPrevious();
        }
    }

    private void addCircleRenderer()
    {
        Paint paint = new Paint();
        paint.setStrokeWidth(3f);
        paint.setAntiAlias(true);
        paint.setColor(Color.argb(255, 222, 92, 143));
    }

    private void addCircleBarRenderer()
    {
        Paint paint = new Paint();
        paint.setStrokeWidth(8f);
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN));
        paint.setColor(Color.argb(255, 222, 92, 143));
    }

    private void addBarGraphRenderers()
    {
        Paint paint = new Paint();
        paint.setStrokeWidth(50f);
        paint.setAntiAlias(true);
        paint.setColor(Color.argb(200, 56, 138, 252));

        Paint paint2 = new Paint();
        paint2.setStrokeWidth(12f);
        paint2.setAntiAlias(true);
        paint2.setColor(Color.argb(200, 181, 111, 233));
    }

}
