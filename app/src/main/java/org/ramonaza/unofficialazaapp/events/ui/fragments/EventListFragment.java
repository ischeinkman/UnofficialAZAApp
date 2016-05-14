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

import org.ramonaza.unofficialazaapp.events.backend.EventDatabaseHandler;
import org.ramonaza.unofficialazaapp.events.backend.services.EventUpdateService;
import org.ramonaza.unofficialazaapp.events.ui.activities.EventPageActivity;
import org.ramonaza.unofficialazaapp.helpers.backend.PreferenceHelper;
import org.ramonaza.unofficialazaapp.helpers.ui.fragments.InfoWrapperListFragStyles.InfoWrapperTextListFragment;
import org.ramonazaapi.events.EventInfoWrapper;
import org.ramonazaapi.interfaces.InfoWrapper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/*
 * Created by Ilan Scheinkman
 */
public class EventListFragment extends InfoWrapperTextListFragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private final long UPDATE_TIMEOUT = 10 * 1000;

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
        if (mWrapper.getId() < 0) return;
        Intent intent = new Intent(getActivity(), EventPageActivity.class);
        intent.putExtra(EventPageActivity.EVENT_DATA, mWrapper.getId());
        startActivity(intent);
    }

    private Observable<Boolean> updateEventsOrTimeOut() {
        final long beginTime = System.currentTimeMillis();
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                while (System.currentTimeMillis() - beginTime < UPDATE_TIMEOUT) {
                    if (serviceBound) {
                        subscriber.onNext(true);
                        subscriber.onCompleted();
                    }
                }
                subscriber.onNext(false);
                subscriber.onCompleted();
            }
        }).flatMap(new Func1<Boolean, Observable<Boolean>>() {
            @Override
            public Observable<Boolean> call(Boolean aBoolean) {
                if (!aBoolean) return Observable.just(false);
                return updateService.updateEvents().map(new Func1<List<EventInfoWrapper>, Boolean>() {
                    @Override
                    public Boolean call(List<EventInfoWrapper> eventInfoWrappers) {
                        return true;
                    }
                });
            }
        });
    }

    @Override
    public Observable<? extends InfoWrapper> generateInfo() {
        String eventFeed = PreferenceHelper.getPreferences(getActivity()).getEventFeed();
        if (eventFeed == null || eventFeed.length() == 0) {
            EventInfoWrapper noFeed = new EventInfoWrapper();
            noFeed.setName("Please download a Chapter Pack to access this feature.");
            noFeed.setId(-1);
            return Observable.just(noFeed);
        }
        final EventInfoWrapper noEvent = new EventInfoWrapper();
        noEvent.setName("No events found.");
        noEvent.setId(-1);
        return updateEventsOrTimeOut().flatMap(new Func1<Boolean, Observable<EventInfoWrapper>>() {
            @Override
            public Observable<EventInfoWrapper> call(Boolean aBoolean) {
                if (!aBoolean) showText("Could not download new events from the server.");
                final DateFormat df = new SimpleDateFormat("EEEE, MMMM dd yyyy");
                final Date current = Calendar.getInstance().getTime();
                return handler.getEvents(null).filter(new Func1<EventInfoWrapper, Boolean>() {
                    @Override
                    public Boolean call(EventInfoWrapper eventInfoWrapper) {
                        Date eventDate = null;
                        try {
                            eventDate = df.parse(eventInfoWrapper.getDate());
                        } catch (ParseException e) {
                            return true;
                        }
                        return eventDate.after(current);
                    }
                }).defaultIfEmpty(noEvent);
            }
        });
    }

}
