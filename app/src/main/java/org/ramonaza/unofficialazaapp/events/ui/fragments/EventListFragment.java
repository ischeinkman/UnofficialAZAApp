package org.ramonaza.unofficialazaapp.events.ui.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ramonaza.unofficialazaapp.events.backend.EventUpdateService;
import org.ramonaza.unofficialazaapp.events.ui.activities.EventPageActivity;
import org.ramonaza.unofficialazaapp.helpers.backend.PreferenceHelper;
import org.ramonaza.unofficialazaapp.helpers.ui.fragments.InfoWrapperListFragStyles.InfoWrapperTextListFragment;
import org.ramonaza.unofficialazaapp.people.backend.EventDatabaseHandler;
import org.ramonazaapi.events.EventInfoWrapper;
import org.ramonazaapi.interfaces.InfoWrapper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/*
 * Created by Ilan Scheinkman
 */
public class EventListFragment extends InfoWrapperTextListFragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private EventDatabaseHandler handler;
    private EventUpdateService updateService;
    private boolean serviceBound;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            updateService = ((EventUpdateService.MyBinder) iBinder).getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceBound = false;
        }
    };

    public static EventListFragment newInstance(int sectionNumber) {
        EventListFragment fragment = new EventListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent(getActivity(), EventUpdateService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (serviceBound) {
            getActivity().unbindService(mConnection);
        }
        serviceBound = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View superView = super.onCreateView(inflater, container, savedInstanceState);
        return superView;
    }

    @Override
    public void onButtonClick(InfoWrapper mWrapper) {
        if (mWrapper.getId() < 0) return;
        Intent intent = new Intent(getActivity(), EventPageActivity.class);
        intent.putExtra(EventPageActivity.EVENT_DATA, mWrapper.getId());
        startActivity(intent);
    }

    @Override
    public InfoWrapper[] generateInfo() {
        String eventFeed = new PreferenceHelper(getActivity()).getEventFeed();
        if (eventFeed == null || eventFeed.length() == 0) {
            EventInfoWrapper noFeed = new EventInfoWrapper();
            noFeed.setName("Please download a Chapter Pack to access this feature.");
            noFeed.setId(-1);
            return new EventInfoWrapper[]{noFeed};
        }
        while (!serviceBound) ;
        updateService.updateEventsSync();
        handler = new EventDatabaseHandler(getActivity());
        //TODO: Store event dates as MYSQL Date objects to allow for WHERE clause filtering
        EventInfoWrapper[] allEvents = handler.getEvents(null, null);
        DateFormat df = new SimpleDateFormat("EEEE, MMMM dd yyyy");
        Date current = Calendar.getInstance().getTime();
        ArrayList futureEvents = new ArrayList();
        for (EventInfoWrapper wrapper : allEvents) {
            try {
                Date eventDate = df.parse(wrapper.getDate());
                if (eventDate.after(current)) futureEvents.add(wrapper);
            } catch (ParseException e) {
                e.printStackTrace();
                futureEvents.add(wrapper); //Just in case someone's date format is off
            }
        }
        if (futureEvents.isEmpty()) {
            EventInfoWrapper noEvent = new EventInfoWrapper();
            noEvent.setName("No events found.");
            noEvent.setId(-1);
            return new EventInfoWrapper[]{noEvent};
        }
        return (EventInfoWrapper[]) futureEvents.toArray(new EventInfoWrapper[futureEvents.size()]);
    }


}
