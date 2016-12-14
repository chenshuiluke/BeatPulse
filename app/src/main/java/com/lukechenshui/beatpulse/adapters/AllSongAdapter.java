package com.lukechenshui.beatpulse.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lukechenshui.beatpulse.Config;
import com.lukechenshui.beatpulse.DrawerInitializer;
import com.lukechenshui.beatpulse.R;
import com.lukechenshui.beatpulse.SharedData;
import com.lukechenshui.beatpulse.Utility;
import com.lukechenshui.beatpulse.models.Playlist;
import com.lukechenshui.beatpulse.models.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

/**
 * Created by luke on 12/11/16.
 */

public class AllSongAdapter extends RecyclerView.Adapter<AllSongAdapter.SongHolder> {
    Context context;
    ArrayList<Song> songs = new ArrayList<>();

    public AllSongAdapter(ArrayList<Song> songs, Context context) {
        this.songs = songs;
        this.context = context;
    }

    @Override
    public void onBindViewHolder(AllSongAdapter.SongHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public AllSongAdapter.SongHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_list_item, parent, false);
        return new AllSongAdapter.SongHolder(view);
    }

    @Override
    public void onBindViewHolder(AllSongAdapter.SongHolder holder, int position) {
        Song song = songs.get(position);
        holder.bindData(song);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    protected class SongHolder extends RecyclerView.ViewHolder {
        TextView songNameTextView;
        Song currentSong;

        public SongHolder(View itemView) {
            super(itemView);
            songNameTextView = (TextView) itemView.findViewById(R.id.fileNameTextView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (currentSong != null) {
                        Intent intent = new Intent(context, DrawerInitializer.getDrawerActivities().get(Config.NOW_PLAYING_DRAWER_ITEM_POS));

                        Playlist playlist = new Playlist(songs, "All Songs");

                        Bundle bundle = new Bundle();
                        bundle.putParcelable("song", currentSong);
                        bundle.putParcelable("playlust", playlist);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtras(bundle);
                        context.startActivity(intent);
                        SharedData.setOrigin(context, "all_songs");
                        ((Activity)context).finish();
                    }
                }
            });
        }

        public void bindData(Song song) {
            if (song != null) {
                currentSong = song;
                songNameTextView.setText(currentSong.getName());
            }
        }
    }
}
