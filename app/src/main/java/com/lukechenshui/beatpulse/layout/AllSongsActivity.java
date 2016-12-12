package com.lukechenshui.beatpulse.layout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.lukechenshui.beatpulse.Config;
import com.lukechenshui.beatpulse.DrawerInitializer;
import com.lukechenshui.beatpulse.R;
import com.lukechenshui.beatpulse.Utility;
import com.lukechenshui.beatpulse.adapters.AllSongAdapter;
import com.lukechenshui.beatpulse.adapters.FileAdapter;
import com.lukechenshui.beatpulse.models.Song;
import com.mikepenz.materialdrawer.Drawer;

import java.io.File;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

public class AllSongsActivity extends AppCompatActivity {
    private LinearLayoutManager layoutManager;
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_songs);


        Toolbar toolbar = (Toolbar) findViewById(R.id.fourthToolbar);
        setSupportActionBar(toolbar);
        Drawer drawer = DrawerInitializer.createDrawer(this, this, toolbar);

        drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        Config.setActiveDrawer(drawer);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView = (RecyclerView) findViewById(R.id.allSongsRecyclerView);
        recyclerView.setLayoutManager(layoutManager);

        Realm realm = Realm.getDefaultInstance();
        RealmResults<Song> songs = realm.where(Song.class).findAll();

        ArrayList<Song> songList = new ArrayList<>();
        songList.addAll(songs);



        AllSongAdapter songAdapter = new AllSongAdapter(songList, this);
        recyclerView.setAdapter(songAdapter);

    }
}
