package com.lukechenshui.beatpulse.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lukechenshui.beatpulse.Config;
import com.lukechenshui.beatpulse.DrawerInitializer;
import com.lukechenshui.beatpulse.R;
import com.lukechenshui.beatpulse.SharedData;
import com.lukechenshui.beatpulse.models.Album;
import com.lukechenshui.beatpulse.models.Song;
import com.vkondrav.swiftadapter.SwiftAdapter;

import io.realm.RealmList;

/**
 * Created by luke on 12/18/16.
 */

public class AlbumAdapter extends SwiftAdapter {
    private final String TAG = "AlbumAdapter";
    private LayoutInflater inflater;
    private RealmList<Album> albums;
    private Album currAlbum;
    private Song currSong;
    private Context context;

    public AlbumAdapter(Context context, RealmList<Album> albums) {
        this.context = context;
        this.albums = albums;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateLvl1SectionViewHolder(ViewGroup parent) {
        return new AlbumViewHolder(inflater.inflate(R.layout.all_album_list_item, parent, false));
    }

    @Override
    public int getNumberOfLvl1Sections() {
        return albums.size();
    }

    @Override
    public void onBindLvl1Section(RecyclerView.ViewHolder holder, final ItemIndex index) {
        super.onBindLvl1Section(holder, index);
        if (holder instanceof AlbumViewHolder) {
            AlbumViewHolder albumHolder = (AlbumViewHolder) holder;
            final Album album = albums.get(index.lvl1Section);
            albumHolder.bindData(album);
            albumHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currAlbum = album;
                    openCloseLvl1Section(index);
                    Log.d(TAG, "Level 1 section was clicked");
                }
            });

        }
    }

    @Override
    public int getNumberOfLvl1ItemsForSection(int lvl1Section) {
        return albums.get(lvl1Section).getSongs().size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateLvl1ItemViewHolder(ViewGroup parent) {
        return new SongViewHolder(inflater.inflate(R.layout.all_album_song_list_item, parent, false));
    }

    @Override
    public void onBindLvl1Item(RecyclerView.ViewHolder holder, ItemIndex index) {
        super.onBindLvl1Item(holder, index);
        if (holder instanceof SongViewHolder) {
            SongViewHolder songHolder = (SongViewHolder) holder;
            final Song song = albums.get(index.lvl1Section).getSongs().get(index.item);
            songHolder.bindData(song);
            songHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currSong = song;
                    SharedData.SongRequest.submitSongRequest(currSong);
                    SharedData.setOrigin(context, currAlbum.getName());
                    Intent intent = new Intent(context, DrawerInitializer.getDrawerActivities().get(Config.NOW_PLAYING_DRAWER_ITEM_POS));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    ((Activity) context).finish();
                }
            });


        }
    }

    public void setData(RealmList<Album> newAlbums) {
        albums.clear();
        albums.addAll(newAlbums);
        notifyDataSetChanged();
    }

    public class AlbumViewHolder extends RecyclerView.ViewHolder {
        TextView albumNameTextView;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            albumNameTextView = (TextView) itemView.findViewById(R.id.albumNameTextView);
        }

        public void bindData(Album album) {
            albumNameTextView.setText(album.getName());
        }
    }

    public class SongViewHolder extends RecyclerView.ViewHolder {
        TextView albumSongNameTextView;

        public SongViewHolder(View itemView) {
            super(itemView);
            albumSongNameTextView = (TextView) itemView.findViewById(R.id.albumSongNameTextView);
        }

        public void bindData(Song song) {
            albumSongNameTextView.setText(song.getName());
        }
    }
}
