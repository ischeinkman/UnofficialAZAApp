package org.ramonazaapi.events;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

/**
 * Created by ilan on 9/8/15.
 */
public class EventRSSHandler {

    private static final String ITEM_SPLITTER = "<item>";
    private static final String ATTRIBUTE_SPLITTER = " <br/> ";
    private static final int TIMEOUT = 2000;
    private String rawRSS;

    public EventRSSHandler(String rssSource, boolean isStream) {
        if (isStream && rssSource != null) rawRSS = getRssFromStream(rssSource);
        else rawRSS = rssSource;
    }

    /**
     * Retrieves the textual RSS source of the events located at the provided URL.
     *
     * @param url the url to retrieve data from
     * @return a string containing the RSS source
     */
    private static String getRssFromStream(String url) {
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
            e.printStackTrace();
        }
        String totalRss = builder.toString();
        String strippedRss;
        if (totalRss.contains(ITEM_SPLITTER)) {
            strippedRss = totalRss.substring(totalRss.indexOf(ITEM_SPLITTER) + ITEM_SPLITTER.length());
        } else strippedRss = null;
        return strippedRss;
    }

    /**
     * Convert the RSS the handler currently has into event objects.
     *
     * @return the converted event objects
     */
    public EventInfoWrapper[] getEventsFromRss() {
        if (rawRSS == null) return new EventInfoWrapper[0];
        String[] itemmedRSS = rawRSS.split(ITEM_SPLITTER);
        EventInfoWrapper[] events = new EventInfoWrapper[itemmedRSS.length];
        for (int i = 0; i < itemmedRSS.length; i++) {
            String[] splitFeed = itemmedRSS[i].split(ATTRIBUTE_SPLITTER);
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
                    currentEvent.setDate(curDateString + Calendar.getInstance().get(Calendar.YEAR));
                    break;
                }
            }
            events[i] = currentEvent;
        }
        return events;
    }

    public String convertEventsToRss() {
        return rawRSS;
    }

    /**
     * Get a specific event from the stored event list via index.
     *
     * @param index the index to retrieve
     * @return the event at that index
     */
    public EventInfoWrapper getEvent(int index) {
        if (rawRSS == null) return null;
        EventInfoWrapper[] allEvents = getEventsFromRss();
        return allEvents[index];
    }

    /**
     * Get the raw RSS of an event from the stored event list via index.
     *
     * @param index the index to retrieve
     * @return the raw RSS of the event at that index
     */
    public String getEventRSS(int index) {
        if (rawRSS == null) return null;
        String[] itemmedRSS = rawRSS.split(ITEM_SPLITTER);
        return itemmedRSS[index];
    }
}
