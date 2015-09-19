package org.ramonaza.unofficialazaapp.events.ui.fragments;

import android.content.Intent;
import android.os.Bundle;

import org.ramonaza.unofficialazaapp.events.backend.EventRSSHandler;
import org.ramonaza.unofficialazaapp.events.ui.activities.EventPageActivity;
import org.ramonaza.unofficialazaapp.helpers.backend.ChapterPackHandlerSupport;
import org.ramonaza.unofficialazaapp.helpers.backend.InfoWrapper;
import org.ramonaza.unofficialazaapp.helpers.ui.fragments.InfoWrapperListFragStyles.InfoWrapperTextListFragment;

/*
 * Created by Ilan Scheinkman
 */
public class EventListFragment extends InfoWrapperTextListFragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private EventRSSHandler handler;




    public static EventListFragment newInstance(int sectionNumber) {
        EventListFragment fragment = new EventListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onButtonClick(InfoWrapper mWrapper) {
        Intent intent=new Intent(getActivity(), EventPageActivity.class);
        intent.putExtra(EventPageActivity.EVENT_DATA, handler.getEventRSS(mWrapper.getId()));
        startActivity(intent);
    }

    @Override
    public InfoWrapper[] generateInfo() {
        ChapterPackHandlerSupport.getChapterPackHandler(getActivity(), ChapterPackHandlerSupport.getOptions()[0]);
        EventRSSHandler handler= ChapterPackHandlerSupport.getEventHandler(getActivity());
        return handler.getEventsFromRss();
    }
}
