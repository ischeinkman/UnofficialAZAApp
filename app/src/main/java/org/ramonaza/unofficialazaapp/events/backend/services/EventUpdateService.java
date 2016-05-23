package org.ramonaza.unofficialazaapp.events.backend.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.events.backend.EventDatabaseHandler;
import org.ramonaza.unofficialazaapp.frontpage.ui.activities.FrontalActivity;
import org.ramonaza.unofficialazaapp.helpers.backend.ChapterPackHandlerSupport;
import org.ramonaza.unofficialazaapp.helpers.backend.PreferenceHelper;
import org.ramonazaapi.events.EventInfoWrapper;
import org.ramonazaapi.events.EventRSSHandler;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

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
    private Subscription updateThread;
    private ConnectableObservable eventObservable;
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

    public static long getLastUpdateTime(Context context){
        return PreferenceHelper.getPreferences(context).getLastEventUpdate();
    }

    private static void updatedEvents(Context context){
        PreferenceHelper.getPreferences(context).setLastEventUpdate(System.currentTimeMillis());
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
        if (updateThread != null) updateThread.unsubscribe();
        updateThread = null;
        eventObservable = null;
        if (isRunning) {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
        isRunning = false;
    }

    public Observable<List<EventInfoWrapper>> updateEvents() {
        Log.v(TAG, "Updating Events");
        if (updateThread == null) {
            eventObservable = createUpdateObservable();
            updateThread = eventObservable.connect();
        }
        return eventObservable.cache();
    }

    private ConnectableObservable<List<EventInfoWrapper>> createUpdateObservable() {

        final NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        final EventDatabaseHandler dbHandler = new EventDatabaseHandler(ChapterPackHandlerSupport.getContactHandler(EventUpdateService.this));

        return Observable.create(new Observable.OnSubscribe<String>() {

            @Override
            public void call(Subscriber<? super String> subscriber) {

                Log.v(TAG, "Service Started");
                isRunning = true;
                String eventFeed = PreferenceHelper.getPreferences(EventUpdateService.this).getEventFeed();

                if (eventFeed == null || eventFeed.length() == 0) {
                    mNotificationManager.cancel(NOTIFICATION_ID);
                    isRunning = false;
                    subscriber.onCompleted();
                } else {
                    subscriber.onNext(eventFeed);
                    subscriber.onCompleted();
                }
            }
        }).flatMap(new Func1<String, Observable<EventInfoWrapper>>() {
            @Override
            public Observable<EventInfoWrapper> call(String eventFeed) {
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                EventRSSHandler rssHandler = new EventRSSHandler(eventFeed, true);
                Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
                mBuilder.setSmallIcon(R.drawable.ic_launcher)
                        .setLargeIcon(largeIcon)
                        .setAutoCancel(false)
                        .setContentTitle("Updating Events")
                        .setContentText("Now downloading events from the event feed...")
                        .setProgress(0, 0, true);
                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
                return rssHandler.connect();
            }
        }).toList().flatMap(new Func1<List<EventInfoWrapper>, Observable<List<EventInfoWrapper>>>() {
            @Override
            public Observable<List<EventInfoWrapper>> call(final List<EventInfoWrapper> rssWrappers) {
                return dbHandler.getEvents(null).toList().map(new Func1<List<EventInfoWrapper>, List<EventInfoWrapper>>() {
                    @Override
                    public List<EventInfoWrapper> call(List<EventInfoWrapper> dbWrappers) {
                        rssWrappers.removeAll(dbWrappers);
                        return rssWrappers;
                    }
                });
            }
        }).flatMap(new Func1<List<EventInfoWrapper>, Observable<EventInfoWrapper>>() {
            @Override
            public Observable<EventInfoWrapper> call(final List<EventInfoWrapper> rss) {
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
                    return dbHandler.deleteEvents(null).toList().flatMap(new Func1<List<Integer>, Observable<EventInfoWrapper>>() {
                        @Override
                        public Observable<EventInfoWrapper> call(List<Integer> integers) {
                            updatedEvents(getApplicationContext());
                            return dbHandler.addEvents(rss);
                        }
                    });
                } else {
                    mNotificationManager.cancel(NOTIFICATION_ID);
                    updatedEvents(getApplicationContext());
                    return Observable.empty();
                }
            }
        }).toList().map(new Func1<List<EventInfoWrapper>, List<EventInfoWrapper>>() {

            @Override
            public List<EventInfoWrapper> call(List<EventInfoWrapper> eventInfoWrappers) {
                EventNotificationService.setUpNotifications(EventUpdateService.this);
                isRunning = false;
                return eventInfoWrappers;
            }
        }).doOnError(new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                mNotificationManager.cancel(NOTIFICATION_ID);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).publish();
    }

    public void restartService() {
        if (isRunning) {
            if (updateThread != null) updateThread.unsubscribe();
            isRunning = false;
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
        eventObservable = createUpdateObservable();
        updateThread = eventObservable.connect();
    }


    public class MyBinder extends Binder {
        public EventUpdateService getService() {
            return EventUpdateService.this;
        }
    }
}

