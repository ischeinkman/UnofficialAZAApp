package org.ramonaza.unofficialazaapp.helpers.ui.fragments.InfoWrapperListFragStyles;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import org.ramonaza.unofficialazaapp.helpers.backend.InfoWrapper;
import org.ramonaza.unofficialazaapp.helpers.ui.fragments.InfoWrapperListFragment;
import org.ramonaza.unofficialazaapp.helpers.ui.other.InfoWrapperTextListAdapter;

/**
 * Parent fragment class for all InfoWrapper top level
 * lists that then lead to detail pages.
 */
public abstract class InfoWrapperTextListFragment extends InfoWrapperListFragment {


    public InfoWrapperTextListFragment() {
        // Required empty public constructor
    }

    @Override
    public ArrayAdapter getAdapter() {
        return new InfoWrapperTextListAdapter(getActivity(), InfoWrapperTextListAdapter.NAME_ONLY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InfoWrapper thisWrapper = (InfoWrapper) listView.getItemAtPosition(position);
                onButtonClick(thisWrapper);
            }
        });
        return rootView;
    }


    /**
     * The action each text view performs.
     *
     * @param mWrapper the button's InfoWrapper; can be cast as necessary
     */
    public abstract void onButtonClick(InfoWrapper mWrapper);


}

