package org.ramonaza.androidzadikapplication.events.ui.fragments;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.ramonaza.androidzadikapplication.events.backend.services.EventUpdateService;
import org.ramonaza.androidzadikapplication.events.ui.activities.EventPageActivity;
import org.ramonaza.androidzadikapplication.helpers.backend.ChapterPackHandlerSupport;
import org.ramonaza.androidzadikapplication.helpers.backend.PreferenceHelper;
import org.ramonaza.androidzadikapplication.helpers.ui.fragments.InfoWrapperListFragStyles.InfoWrapperTextListFragment;
import org.ramonaza.androidzadikapplication.people.backend.EventDatabaseHandler;
import org.ramonaza.androidzadikapplication.settings.ui.activities.SettingsActivity;
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
    private boolean readAccessRequested = false;

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
        if (serviceBound) {
            getActivity().unbindService(mConnection);
        }
        serviceBound = false;
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View superView = super.onCreateView(inflater, container, savedInstanceState);
        return superView;
    }

    @Override
    public void onButtonClick(InfoWrapper mWrapper) {
        if (mWrapper.getId() < 0) {
            if (readAccessRequested)
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, SettingsActivity.FILE_READ);
            return;
        }
        Intent intent = new Intent(getActivity(), EventPageActivity.class);
        intent.putExtra(EventPageActivity.EVENT_DATA, mWrapper.getId());
        startActivity(intent);
    }

    @Override
    public InfoWrapper[] generateInfo() {
        // Automatically searches for a chapter pack if there are none already loaded.
        if (!ChapterPackHandlerSupport.chapterPackIsLoaded() && ChapterPackHandlerSupport.getOptions().length > 0)
            ChapterPackHandlerSupport.getChapterPackHandler(getActivity(), ChapterPackHandlerSupport.getOptions()[0]);

        String eventFeed = PreferenceHelper.getPreferences(getActivity()).getEventFeed();
        if (eventFeed == null || eventFeed.length() == 0) {
            EventInfoWrapper noFeed = new EventInfoWrapper();
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (!readAccessRequested) {
                    readAccessRequested = true;
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, SettingsActivity.FILE_READ);
                }
                noFeed.setName("Please allow file read access to load your Chapter Pack.");
            } else {
                noFeed.setName("Please download a Chapter Pack to access this feature.");
            }
            noFeed.setId(-1);
            return new EventInfoWrapper[]{noFeed};
        }
        final long TIMEOUT = 10 * 1000;
        long beginTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - beginTime < TIMEOUT) {
            if (serviceBound) {
                updateService.updateEventsSync();
                break;
            }
        }
        if (!serviceBound) {
            Toast.makeText(getActivity(), "Could not connect to server in time.", Toast.LENGTH_SHORT);
            Log.d("EventListFrag", "Service not bound in time");
        }
        handler = new EventDatabaseHandler(getActivity());
        //TODO: Store event dates as MYSQL Date objects to allow for WHERE clause filtering
        EventInfoWrapper[] allEvents = handler.getEvents(null, null);
        DateFormat df = new SimpleDateFormat("EEEE, MMMM dd yyyy");
        Date yesterday = new Date(System.currentTimeMillis() - 1000L * 60L * 60L * 24L);
        ArrayList futureEvents = new ArrayList();
        for (EventInfoWrapper wrapper : allEvents) {
            try {
                Date eventDate = df.parse(wrapper.getDate());
                if (eventDate.after(yesterday)) futureEvents.add(wrapper);
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
