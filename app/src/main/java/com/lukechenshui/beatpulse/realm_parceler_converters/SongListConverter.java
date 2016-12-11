package com.lukechenshui.beatpulse.realm_parceler_converters;

import android.os.Parcel;

import com.lukechenshui.beatpulse.models.Song;

import org.parceler.Parcels;

/**
 * Created by luke on 12/11/16.
 */

public class SongListConverter extends RealmListParcelConverter<Song> {

    @Override
    public void itemToParcel(Song input, Parcel parcel) {
        parcel.writeParcelable(Parcels.wrap(input), 0);
    }

    @Override
    public Song itemFromParcel(Parcel parcel) {
        return Parcels.unwrap(parcel.readParcelable(Song.class.getClassLoader()));
    }
}
