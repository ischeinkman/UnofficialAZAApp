package org.ramonaza.unofficialazaapp.songs.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.ramonaza.unofficialazaapp.frontpage.ui.activities.FrontalActivity;
import org.ramonaza.unofficialazaapp.helpers.backend.InfoWrapper;
import org.ramonaza.unofficialazaapp.helpers.ui.fragments.InfoWrapperListFragStyles.InfoWrapperTextListFragment;
import org.ramonaza.unofficialazaapp.songs.backend.SongInfoWrapperGenerator;
import org.ramonaza.unofficialazaapp.songs.ui.activities.SongDataActivity;

/**
 * The fragment containing the list of songs.
 * Created by ilanscheinkman on 1/9/15.
 */

public class SongListFragment extends InfoWrapperTextListFragment {

    public static final String EXTRA_CONTRUCTION_INFO="org.ramonaza.unofficialazaapp.CONSTRUCTION_INFO";
    public static final String EXTRA_LAYER="org.ramonaza.unofficialazaapp.LAYER_NAME";

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static int fragLayer; //int to store the position in

    public static SongListFragment newInstance(int sectionNumber) {
        SongListFragment fragment = new SongListFragment();
        Bundle args = new Bundle();
        fragLayer = sectionNumber;
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((FrontalActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onButtonClick(InfoWrapper mWrapper) {
        Intent intent=new Intent(getActivity(), SongDataActivity.class);
        intent.putExtra(EXTRA_CONTRUCTION_INFO,mWrapper.getName());
        startActivity(intent);
    }

    @Override
    public InfoWrapper[] generateInfo() {
        return SongInfoWrapperGenerator.allSongs(getActivity());
    }



}