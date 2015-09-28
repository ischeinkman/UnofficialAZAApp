package org.ramonaza.unofficialazaapp.events.ui.fragments;

import android.content.Intent;
import android.os.Bundle;

import org.ramonaza.unofficialazaapp.events.backend.EventInfoWrapper;
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
        if(mWrapper.getId() <0) return;
        Intent intent=new Intent(getActivity(), EventPageActivity.class);
        intent.putExtra(EventPageActivity.EVENT_DATA, handler.getEventRSS(mWrapper.getId()));
        startActivity(intent);
    }

    @Override
    public InfoWrapper[] generateInfo() {
        if(ChapterPackHandlerSupport.getOptions().length > 0){
            ChapterPackHandlerSupport.getChapterPackHandler(getActivity(), ChapterPackHandlerSupport.getOptions()[0]);
        }
        handler= ChapterPackHandlerSupport.getEventHandler(getActivity());
        if(handler == null){
            EventInfoWrapper noLoadedHandler=new EventInfoWrapper();
            noLoadedHandler.setName("Please download a Chapter Pack to access this feature.");
            noLoadedHandler.setId(-1);
            return new EventInfoWrapper[]{noLoadedHandler};
        }
        EventInfoWrapper[] handlerEvents=handler.getEventsFromRss();
        if(handlerEvents == null){
            EventInfoWrapper nullEvent=new EventInfoWrapper();
            nullEvent.setName("An error occured. Please try again later.");
            nullEvent.setId(-1);
            return new EventInfoWrapper[]{nullEvent};
        }
        else if(handlerEvents.length == 0){
            EventInfoWrapper noEvent=new EventInfoWrapper();
            noEvent.setName("No events posted.");
            noEvent.setId(-1);
            return new EventInfoWrapper[]{noEvent};
        }
        else return handlerEvents;
    }


}
