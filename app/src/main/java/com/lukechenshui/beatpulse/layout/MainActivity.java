package com.lukechenshui.beatpulse.layout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.lukechenshui.beatpulse.Config;
import com.lukechenshui.beatpulse.DrawerInitializer;
import com.lukechenshui.beatpulse.ExternalStorage;
import com.lukechenshui.beatpulse.R;
import com.lukechenshui.beatpulse.SharedData;
import com.lukechenshui.beatpulse.Utility;
import com.lukechenshui.beatpulse.adapters.AlbumAdapter;
import com.lukechenshui.beatpulse.models.Album;
import com.lukechenshui.beatpulse.models.Playlist;
import com.lukechenshui.beatpulse.models.Song;
import com.lukechenshui.beatpulse.services.MusicService;
import com.mikepenz.materialdrawer.Drawer;

import java.io.File;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class MainActivity extends ActionBarActivity {
    private static final String TAG = "MainActivity";
    private static boolean firstRun = true;
    private final int REQUEST_PERMISSIONS = 15;
    AlbumAdapter albumAdapter;
    private RecyclerView albumList;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSIONS:
                if(grantResults.length > 0){
                    for(int result : grantResults){
                        if(result != PackageManager.PERMISSION_GRANTED){
                            finish();
                        }
                    }
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Permission granted!");
                        init();
                    } else {
                        Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Permission denied!");
                    }
                }

                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Realm.init(getApplicationContext());
        setContentView(R.layout.activity_main);

        albumList = (RecyclerView) findViewById(R.id.albumList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        albumList.setLayoutManager(layoutManager);
        populateAlbumList();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);
        }
        else{
            init();
        }

        startService(new Intent(this, MusicService.class));

    }

    private void init(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Drawer drawer = DrawerInitializer.createDrawer(this, this, toolbar);

        drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        Config.setActiveDrawer(drawer);


        Realm realm = Realm.getDefaultInstance();
        RealmResults<Song> results = realm.where(Song.class).findAll();
        drawer.setSelection(Config.ALBUM_DRAWER_ITEM_POS + 1, false);


        if(firstRun){
            scanForMusic();
            firstRun = false;

            if(Config.getLastSong(getApplicationContext()) != null){
                drawer.setSelection(Config.NOW_PLAYING_DRAWER_ITEM_POS+1, true);
            } else {
                drawer.setSelection(Config.ALL_SONGS_DRAWER_ITEM_POS+1, true);
            }

        }
        populateAlbumList();
    }
    public void scanForMusic(){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(Song.class);
        realm.delete(Playlist.class);
        File root = new File("/");

        String[] storageDirs = ExternalStorage.getExternalStorageDirectories(this);
        for (String storageDirName : storageDirs) {
            File storageDir = new File(storageDirName);
            ArrayList<File> fileList = Utility.getListOfFoldersAndAudioFilesInDirectory(getApplicationContext(),
                    storageDir);

            for(File file : fileList){
                Log.d(TAG, "Found song: " + file);
                Song song = new Song(file);
                realm.copyToRealmOrUpdate(song);
            }
        }

        realm.commitTransaction();
        realm.beginTransaction();
        SharedData.init();
        RealmList<Song> allSongs = SharedData.getAllSongs();

        for (Song song : allSongs) {
            String albumName = song.getAlbum();
            Album album = realm.where(Album.class).equalTo("name", albumName).findFirst();

            if (album == null) {
                album = realm.createObject(Album.class, albumName);
            }
            album.addSong(song);
        }
        realm.commitTransaction();
        SharedData.init();

    }

    private void populateAlbumList() {
        if (albumAdapter == null) {
            albumAdapter = new AlbumAdapter(this, SharedData.getAllAlbums());
            albumList.setAdapter(albumAdapter);
        } else {
            albumAdapter.setData(SharedData.getAllAlbums());
        }
    }
}
