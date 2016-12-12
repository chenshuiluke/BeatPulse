package com.lukechenshui.beatpulse.layout;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.lukechenshui.beatpulse.Config;
import com.lukechenshui.beatpulse.DrawerInitializer;
import com.lukechenshui.beatpulse.R;
import com.lukechenshui.beatpulse.models.Song;
import com.lukechenshui.beatpulse.services.MusicService;
import com.lukechenshui.beatpulse.visualizer.VisualizerView;
import com.lukechenshui.beatpulse.visualizer.renderer.CircleRenderer;
import com.lukechenshui.beatpulse.visualizer.renderer.LineRenderer;
import com.mikepenz.materialdrawer.Drawer;

import at.markushi.ui.CircleButton;

public class PlayActivity extends ActionBarActivity {
    private final String TAG = "PlayActivity";
    private CircleButton previousSongButton;
    private CircleButton playOrPauseButton;
    private CircleButton nextSongButton;
    private MusicService musicService;
    private VisualizerView visualizerView;

    private Song currentSong;

    private boolean bound;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("MEDIA_PLAYER_PAUSED")){
                playOrPauseButton.setImageResource(R.drawable.ic_play_arrow_white_48dp);
            }
            else if(intent.getAction().equals("MEDIA_PLAYER_STARTED")){
                playOrPauseButton.setImageResource(R.drawable.ic_pause_white_48dp);
            }
            Log.d(TAG, "Received broadcast: " + intent.getAction());
        }
    };
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)iBinder;
            musicService = binder.getService();
            musicService.init(currentSong);
            try{
                visualizerView.link(musicService.getPlayer());
            }
            catch (IllegalStateException exc){
                Log.d(TAG, "Exception when starting visualization", exc);
            }

            addCircleRenderer();

            bound = true;
            Log.d(TAG, "Connected to music service");
            musicService.play();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bound = false;
            visualizerView.release();
            Log.d(TAG, "Disconnected from music service");
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        if(bound){
            unbindService(connection);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        visualizerView = (VisualizerView) findViewById(R.id.visualizerView);
        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(getApplicationContext());

        IntentFilter filter = new IntentFilter();
        filter.addAction("MEDIA_PLAYER_PAUSED");
        filter.addAction("MEDIA_PLAYER_STARTED");

        bManager.registerReceiver(receiver, filter);




        previousSongButton = (CircleButton) findViewById(R.id.previousSongButton);
        playOrPauseButton = (CircleButton) findViewById(R.id.playOrPauseButton);
        nextSongButton = (CircleButton) findViewById(R.id.nextSongButton);

        Toolbar toolbar = (Toolbar) findViewById(R.id.thirdToolbar);
        setSupportActionBar(toolbar);
        Drawer drawer = DrawerInitializer.createDrawer(this, this, toolbar);

        drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        Config.setActiveDrawer(drawer);

        Song song = getIntent().getParcelableExtra("song");
        currentSong = song;
        if(song != null){
            Intent intent = new Intent(this, MusicService.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("song", song);
            intent.putExtras(bundle);
            bindService(intent, connection, BIND_AUTO_CREATE);
        }
    }

    public void startOrPauseMediaPlayer(View view){
        if(musicService != null){
            if(musicService.getPlayer().isPlaying()){
                musicService.pause();
            }
            else if(!musicService.getPlayer().isPlaying()){
                musicService.play();
            }
        }
    }

    private void addCircleRenderer()
    {
        Paint paint = new Paint();
        paint.setStrokeWidth(3f);
        paint.setAntiAlias(true);
        paint.setColor(Color.argb(255, 222, 92, 143));
        CircleRenderer circleRenderer = new CircleRenderer(paint, true);
        visualizerView.addRenderer(circleRenderer);
    }

}
