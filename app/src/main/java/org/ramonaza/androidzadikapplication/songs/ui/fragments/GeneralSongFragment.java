package org.ramonaza.androidzadikapplication.songs.ui.fragments;

import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ramonaza.androidzadikapplication.R;
import org.ramonaza.androidzadikapplication.songs.backend.SongGenderedConstants;
import org.ramonazaapi.songs.SongInfoWrapper;

/**
 * General Song Text Class.
 * Created by Ilan Scheinkman on 1/9/15.
 */
public class GeneralSongFragment extends Fragment {

    private static final String SONG_TITLE = "songtitle";
    private SongInfoWrapper mySong;

    public GeneralSongFragment() {
    }

    public static GeneralSongFragment newInstance(SongInfoWrapper song) {
        GeneralSongFragment fragment = new GeneralSongFragment();
        Bundle args = new Bundle();
        args.putString(SONG_TITLE, song.getName());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setTitle(mySong.getName());
        View rootView = inflater.inflate(R.layout.fragment_song_data, container, false);
        TextView songText = (TextView) rootView.findViewById(R.id.songTextView);
        songText.setText(mySong.getLyrics());
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mySong = SongGenderedConstants.SONG_LIST.getSong(savedInstanceState.getString(SONG_TITLE));
        } else {
            mySong = SongGenderedConstants.SONG_LIST.getSong(getArguments().getString(SONG_TITLE));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SONG_TITLE, mySong.getName());
    }
}