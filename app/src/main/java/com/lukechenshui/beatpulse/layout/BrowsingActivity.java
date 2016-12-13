package com.lukechenshui.beatpulse.layout;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.lukechenshui.beatpulse.Config;
import com.lukechenshui.beatpulse.DrawerInitializer;
import com.lukechenshui.beatpulse.R;
import com.lukechenshui.beatpulse.Utility;
import com.lukechenshui.beatpulse.adapters.FileAdapter;
import com.mikepenz.materialdrawer.Drawer;

import java.io.File;
import java.util.ArrayList;

public class BrowsingActivity extends ActionBarActivity {
    private LinearLayoutManager layoutManager;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browsing);

        Toolbar toolbar = (Toolbar) findViewById(R.id.secondToolbar);
        setSupportActionBar(toolbar);
        Drawer drawer = DrawerInitializer.createDrawer(this, this, toolbar);

        drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        Config.setActiveDrawer(drawer);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView = (RecyclerView) findViewById(R.id.browsingRecyclerView);
        recyclerView.setLayoutManager(layoutManager);
        ArrayList<File> files = Utility.getListOfFoldersAndAudioFilesInDirectoryWithParent(getApplicationContext());

        FileAdapter fileAdapter = new FileAdapter(files, this);
        recyclerView.setAdapter(fileAdapter);
    }
}
