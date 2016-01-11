package org.ramonaza.unofficialazaapp.helpers.backend;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.database.AppDatabaseHelper;
import org.ramonaza.unofficialazaapp.frontpage.ui.activities.FrontalActivity;
import org.ramonaza.unofficialazaapp.people.backend.EventDatabaseHandler;
import org.ramonazaapi.events.EventInfoWrapper;
import org.ramonazaapi.events.EventRSSHandler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Yuval Zach aka kingi2001 on 12/29/2015.
 */

/**
 * This class is meant to retrieve events from the Chapter's website and store them in a local database.
 * Additionally, this class will send notifications in case there is a new event on the website, and will
 * send notifications a certain time before the event.
 */

public class EventNotificationService extends Service {

    private static final String TAG = "EventNotifService";
    private static final int NOTIFICATION_ID = 8888;
    private static boolean isRepeating = false;
    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(EventNotificationService.this);
    private boolean isRunning  = false;
    private Thread updateThread;
    private MyBinder binder = new MyBinder();

    public static void startRepeater(Context context) {
        if (isRepeating) return;
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, EventNotificationService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
        mgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, AlarmManager.INTERVAL_HALF_HOUR, AlarmManager.INTERVAL_HALF_HOUR, pi);
        isRepeating = true;
    }

    public static void cancelRepeater(Context context) {
        if (!isRepeating) return;
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, EventNotificationService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
        mgr.cancel(pi);
        isRepeating = false;
    }

    public void onCreate() {
        Log.i(TAG, "Service onCreate");

    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service onStartCommand");
        restartService();
        startRepeater(this);
        return Service.START_NOT_STICKY;
    }

    public IBinder onBind(Intent arg0) {
        Log.i(TAG, "Service onBind");
        startRepeater(this);
        return binder;
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        if (updateThread != null) updateThread.interrupt();
        updateThread = null;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);
        Log.i(TAG, "Service onDestroy");
    }

    /**
     * Synchronously updates events. Note that if we are already attempting
     * to update events in the background the previous thread is interrupted.
     */
    public void updateEventsSync() {
        if (isRunning) {
            updateThread.interrupt();
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
        updateEvents();
    }

    private void updateEvents() {
        Log.v(TAG, "Service Started");
        isRunning = true;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        AppDatabaseHelper ap = new AppDatabaseHelper(EventNotificationService.this);
        SQLiteDatabase myDB = ap.getWritableDatabase();
        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        String eventFeed = prefs.getString(ChapterPackHandlerSupport.PREF_EVENT_FEED, "");

        if (eventFeed == null || eventFeed.length() == 0) {
            mNotificationManager.cancel(NOTIFICATION_ID);
            isRunning = false;
            return;
        }
        EventRSSHandler rssHandler = new EventRSSHandler(eventFeed, true);
        EventDatabaseHandler dbHandler = new EventDatabaseHandler(myDB);

        mBuilder.setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(false)
                .setContentTitle("Updating Events")
                .setContentText("Now downloading events from the event feed...")
                .setProgress(0, 0, true);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

        //Retrieve events from RSS and Database.
        EventInfoWrapper[] rssEvents = rssHandler.getEventsFromRss();
        if (rssEvents.length == 0) {
            mBuilder.setContentTitle("No new events found.")
                    .setContentText("")
                    .setAutoCancel(true)
                    .setProgress(0, 0, false);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            isRunning = false;
            return;
        }

        EventInfoWrapper[] dbEvents = dbHandler.getEvents(null, null);
        // Send notification
        Set<EventInfoWrapper> rss = new HashSet<>(Arrays.asList(rssEvents));
        Set<EventInfoWrapper> db = new HashSet<>(Arrays.asList(dbEvents));

        if (db.size() > 0) {
            rss.removeAll(db);
        }

        if (rss.size() > 0) {
            String title = (rss.size() == 1) ? "You have 1 new event!" : "You have " + rss.size() + " new events!";
            mBuilder.setContentTitle(title)
                    .setContentText("Tap to see details.").setAutoCancel(true)
                    .setProgress(0, 0, false);
            mBuilder.setDefaults(Notification.DEFAULT_ALL);

            Intent resultIntent = new Intent(EventNotificationService.this, FrontalActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(EventNotificationService.this);
            stackBuilder.addParentStack(FrontalActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(EventNotificationService.this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }

        dbHandler.deleteEvents(null, null);
        for (EventInfoWrapper e : rssEvents) {
            try {
                dbHandler.addEvent(e);
            } catch (EventDatabaseHandler.EventCSVReadError eventCSVReadError) {
                eventCSVReadError.printStackTrace();
            }
        }

        myDB.close();
        isRunning = false;
    }

    private Thread createNewUpdateThread() {
        updateThread = new Thread(new Runnable() {
            public void run() {
                updateEvents();
                stopSelf();
            }
        });
        return updateThread;
    }

    public void restartService() {
        if (isRunning) {
            updateThread.interrupt();
            isRunning = false;
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
        createNewUpdateThread().start();
    }


    public class MyBinder extends Binder {
        public EventNotificationService getService() {
            return EventNotificationService.this;
        }
    }
}

