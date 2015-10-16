package org.ramonaza.unofficialazaapp.songs.ui.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ramonaza.unofficialazaapp.R;
import org.ramonazaapi.songs.SongInfoWrapper;

/**
 * General Song Text Class.
 * Created by Ilan Scheinkman on 1/9/15.
 */
public class GeneralSongFragment extends Fragment {

    private SongInfoWrapper mySong;

    public GeneralSongFragment() {
    }

    public static GeneralSongFragment newInstance(SongInfoWrapper song) {
        GeneralSongFragment fragment = new GeneralSongFragment();
        Bundle args = new Bundle();
        fragment.setSong(song);
        fragment.setArguments(args);
        return fragment;
    }

    public void setSong(SongInfoWrapper songInfoWrapper) {
        this.mySong = songInfoWrapper;
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

}