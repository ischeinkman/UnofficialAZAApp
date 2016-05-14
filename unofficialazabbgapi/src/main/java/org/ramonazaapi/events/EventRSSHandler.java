package org.ramonazaapi.events;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by ilan on 9/8/15.
 */
public class EventRSSHandler {

    private static final String ITEM_SPLITTER = "<item>";
    private static final String ATTRIBUTE_SPLITTER = " <br/> ";
    private static final int TIMEOUT = 2000;

    private List<EventInfoWrapper> allEvents;
    private String source;
    private boolean isStream;
    private boolean isConnected;

    public EventRSSHandler(String rssSource, boolean isStream) {
        this.source = rssSource;
        this.isStream = isStream;
        allEvents = new ArrayList<>();
        isConnected = false;

    }

    /**
     * Retrieves the textual RSS source of the events located at the provided URL.
     *
     * @param url the url to retrieve data from
     * @return a string containing the RSS source
     */
    private static Observable<String> getRssFromStream(final String url) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                StringBuilder builder = new StringBuilder(100000);
                DefaultHttpClient client = new DefaultHttpClient();
                client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT);
                client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT);
                HttpGet httpGet = new HttpGet(url);
                try {
                    HttpResponse execute = client.execute(httpGet);
                    InputStream content = execute.getEntity().getContent();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s;
                    while ((s = buffer.readLine()) != null) {
                        builder.append(s);
                    }

                } catch (Exception e) {
                    subscriber.onError(e);
                }
                String totalRss = builder.toString();
                String strippedRss;
                if (totalRss.contains(ITEM_SPLITTER)) {
                    strippedRss = totalRss.substring(totalRss.indexOf(ITEM_SPLITTER) + ITEM_SPLITTER.length());
                } else strippedRss = null;
                subscriber.onNext(strippedRss);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    public boolean isConnected() {
        return isConnected;
    }

    public Observable<EventInfoWrapper> connect() {
        isConnected = true;
        if (isStream && source != null) {
            return getRssFromStream(source).flatMap(new Func1<String, Observable<EventInfoWrapper>>() {
                @Override
                public Observable<EventInfoWrapper> call(String s) {
                    return getEventsFromRss(s);
                }
            }).map(new Func1<EventInfoWrapper, EventInfoWrapper>() {
                @Override
                public EventInfoWrapper call(EventInfoWrapper eventInfoWrapper) {
                    allEvents.add(eventInfoWrapper);
                    return eventInfoWrapper;
                }
            }).subscribeOn(Schedulers.io());
        } else {
            return getEventsFromRss(source).map(new Func1<EventInfoWrapper, EventInfoWrapper>() {
                @Override
                public EventInfoWrapper call(EventInfoWrapper eventInfoWrapper) {
                    allEvents.add(eventInfoWrapper);
                    return eventInfoWrapper;
                }
            }).subscribeOn(Schedulers.io());
        }
    }

    /**
     * Convert the RSS the handler currently has into event objects.
     *
     * @return the converted event objects
     */
    private Observable<EventInfoWrapper> getEventsFromRss(String rawRSS) {
        if (rawRSS == null) return Observable.empty();
        return Observable.from(rawRSS.split(ITEM_SPLITTER)).map(new Func1<String, EventInfoWrapper>() {
            int i = 0;

            @Override
            public EventInfoWrapper call(String s) {
                String[] splitFeed = s.split(ATTRIBUTE_SPLITTER);
                EventInfoWrapper currentEvent = new EventInfoWrapper();
                currentEvent.setDate(splitFeed[1]);
                currentEvent.setName(splitFeed[2]);
                currentEvent.setDesc(splitFeed[3]);
                currentEvent.setMeet(splitFeed[5] + " @ " + splitFeed[4]);
                currentEvent.setBring(splitFeed[6]);
                currentEvent.setPlanner(splitFeed[7]);
                currentEvent.setMapsLocation(splitFeed[8]);
                currentEvent.setId(i);
                String curDateString = currentEvent.getDate();
                for (int ind = curDateString.length() - 4; ind < curDateString.length(); ind++) {
                    if (!Character.isDigit(curDateString.charAt(ind))) {
                        currentEvent.setDate(curDateString + " " + Calendar.getInstance().get(Calendar.YEAR));
                        break;
                    }
                }
                i++;
                return currentEvent;
            }
        }).subscribeOn(Schedulers.io());
    }

    /**
     * Get a specific event from the stored event list via index.
     *
     * @param index the index to retrieve
     * @return the event at that index
     */
    public EventInfoWrapper getStoredEvent(int index) {
        if (allEvents == null || allEvents.size() < index - 1) return null;
        return allEvents.get(index);
    }

    public List<EventInfoWrapper> getStoredEvents() {
        return allEvents;
    }
}
