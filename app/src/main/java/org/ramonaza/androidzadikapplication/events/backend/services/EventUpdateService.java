package org.ramonaza.androidzadikapplication.events.backend.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.ramonaza.androidzadikapplication.R;
import org.ramonaza.androidzadikapplication.database.AppDatabaseHelper;
import org.ramonaza.androidzadikapplication.frontpage.ui.activities.FrontalActivity;
import org.ramonaza.androidzadikapplication.helpers.backend.PreferenceHelper;
import org.ramonaza.androidzadikapplication.people.backend.EventDatabaseHandler;
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
 */

public class EventUpdateService extends Service {

    private static final String TAG = "EventUpdateService";
    private static final int NOTIFICATION_ID = 8888;
    private static final long TIME_MULTIPLIER = 1000 * 60;
    private static boolean isRepeating = false;
    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(EventUpdateService.this);
    private boolean isRunning  = false;
    private Thread updateThread;
    private MyBinder binder = new MyBinder();
    private boolean isBound = false;

    public static void startRepeater(Context context) {
        long interval = PreferenceHelper.getPreferences(context).getEventUpdateTime() * TIME_MULTIPLIER;
        if (interval < 0l) {
            cancelRepeater(context);
            return;
        }
        if (isRepeating) return;
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, EventUpdateService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
        mgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, interval, interval, pi);
        isRepeating = true;
    }

    public static void cancelRepeater(Context context) {
        if (!isRepeating) return;
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, EventUpdateService.class);
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
        isBound = true;
        stopRunning();
        return binder;
    }

    @Override
    public void onDestroy() {
        stopRunning();
        Log.i(TAG, "Service onDestroy");
    }

    public void stopRunning() {
        if (updateThread != null) updateThread.interrupt();
        updateThread = null;
        if (isRunning) {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
        isRunning = false;
    }

    /**
     * Synchronously updates events. Note that if we are already attempting
     * to update events in the background the previous thread is interrupted.
     */
    public void updateEventsSync() {
        if (isRunning) stopRunning();
        updateEvents();
    }

    private void updateEvents() {
        Log.v(TAG, "Service Started");
        isRunning = true;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        AppDatabaseHelper ap = new AppDatabaseHelper(EventUpdateService.this);
        SQLiteDatabase myDB = ap.getWritableDatabase();
        String eventFeed = PreferenceHelper.getPreferences(this).getEventFeed();

        if (eventFeed == null || eventFeed.length() == 0) {
            mNotificationManager.cancel(NOTIFICATION_ID);
            isRunning = false;
            return;
        }
        EventRSSHandler rssHandler = new EventRSSHandler(eventFeed, true);
        EventDatabaseHandler dbHandler = new EventDatabaseHandler(myDB);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        mBuilder.setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(largeIcon)
                .setAutoCancel(false)
                .setContentTitle("Updating Events")
                .setContentText("Now downloading events from the event feed...")
                .setProgress(0, 0, true);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

        //Retrieve events from RSS and Database.
        EventInfoWrapper[] rssEvents = rssHandler.getEventsFromRss();
        if (rssEvents.length == 0) {
            mNotificationManager.cancel(NOTIFICATION_ID);
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

            Intent resultIntent = new Intent(EventUpdateService.this, FrontalActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(EventUpdateService.this);
            stackBuilder.addParentStack(FrontalActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(EventUpdateService.this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        } else {
            mNotificationManager.cancel(NOTIFICATION_ID);
            return;
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
        EventNotificationService.setUpNotifications(this);
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
            if (updateThread != null) updateThread.interrupt();
            isRunning = false;
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
        createNewUpdateThread().start();
    }


    public class MyBinder extends Binder {
        public EventUpdateService getService() {
            return EventUpdateService.this;
        }
    }
}

