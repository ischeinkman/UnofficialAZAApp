package org.ramonaza.unofficialazaapp.helpers.backend;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.database.AppDatabaseHelper;
import org.ramonaza.unofficialazaapp.frontpage.ui.activities.FrontalActivity;
import org.ramonaza.unofficialazaapp.people.backend.EventDatabaseHandler;
import org.ramonazaapi.events.EventInfoWrapper;
import org.ramonazaapi.events.EventRSSHandler;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

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
    private boolean isRunning  = false;


    public void onCreate() {
        Log.i(TAG, "Service onCreate");
        isRunning = true;
      
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service onStartCommand");
        new Thread(new Runnable() {
            public void run() {
                Log.v(TAG, "Service Started");
                    AppDatabaseHelper ap = new AppDatabaseHelper(EventNotificationService.this);
                    SQLiteDatabase myDB = ap.getWritableDatabase();
                    SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
                    String eventFeed = prefs.getString("PREF_EVENT_FEED", "");

                    // Avoid NullPointerException in case eventFeed is not found.
                    if (eventFeed != null && eventFeed.length() > 0){
                        EventRSSHandler rssHandler = new EventRSSHandler(eventFeed, true);
                        EventDatabaseHandler dbHandler = new EventDatabaseHandler(myDB);

                        //Retrieve events from RSS and Database.
                        EventInfoWrapper[] dbEvents =  dbHandler.getEvents(null, null);
                        EventInfoWrapper[] rssEvents = rssHandler.getEventsFromRss();

                        // Send notification
                        ArrayList<EventInfoWrapper> rss = new ArrayList<EventInfoWrapper>(Arrays.asList(rssEvents));
                        ArrayList<EventInfoWrapper> db = new ArrayList<EventInfoWrapper>(Arrays.asList(dbEvents));
                        if(db.size() > 0) {
                            rss.removeAll(db);
                            if (rss.size() > 1) {
                                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(EventNotificationService.this)
                                        .setSmallIcon(R.drawable.ic_launcher)
                                        .setContentTitle("You have " + rss.size() + " new events!")
                                        .setContentText("Tap to see details.").setAutoCancel(true);
                                mBuilder.setDefaults(Notification.DEFAULT_ALL);

                                Intent resultIntent = new Intent(EventNotificationService.this, FrontalActivity.class);
                                TaskStackBuilder stackBuilder = TaskStackBuilder.create(EventNotificationService.this);
                                stackBuilder.addParentStack(FrontalActivity.class);
                                stackBuilder.addNextIntent(resultIntent);
                                PendingIntent resultPendingIntent = PendingIntent.getActivity(EventNotificationService.this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                mBuilder.setContentIntent(resultPendingIntent);
                                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                mNotificationManager.notify(1336, mBuilder.build());


                            } else if (rss.size() == 1) {
                                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(EventNotificationService.this)
                                        .setSmallIcon(R.drawable.ic_launcher)
                                        .setContentTitle("You have 1 new event!")
                                        .setContentText(rss.get(0).getName()).setAutoCancel(true);
                                mBuilder.setDefaults(Notification.DEFAULT_ALL);

                                Intent resultIntent = new Intent(EventNotificationService.this, FrontalActivity.class);
                                TaskStackBuilder stackBuilder = TaskStackBuilder.create(EventNotificationService.this);
                                stackBuilder.addParentStack(FrontalActivity.class);
                                stackBuilder.addNextIntent(resultIntent);
                                PendingIntent resultPendingIntent = PendingIntent.getActivity(EventNotificationService.this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                mBuilder.setContentIntent(resultPendingIntent);
                                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                mNotificationManager.notify(1337, mBuilder.build());

                            }

                        }
                        else{
                            if (rss.size() > 1) {
                                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(EventNotificationService.this)
                                        .setSmallIcon(R.drawable.ic_launcher)
                                        .setContentTitle("You have " + rss.size() + " new events!")
                                        .setContentText("Tap to see details.").setAutoCancel(true);
                                mBuilder.setDefaults(Notification.DEFAULT_ALL);

                                mBuilder.setDefaults(Notification.DEFAULT_ALL);
                                Intent resultIntent = new Intent(EventNotificationService.this, FrontalActivity.class);
                                TaskStackBuilder stackBuilder = TaskStackBuilder.create(EventNotificationService.this);
                                stackBuilder.addParentStack(FrontalActivity.class);
                                stackBuilder.addNextIntent(resultIntent);
                                PendingIntent resultPendingIntent = PendingIntent.getActivity(EventNotificationService.this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                mBuilder.setContentIntent(resultPendingIntent);
                                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                mNotificationManager.notify(1338, mBuilder.build());


                            } else if (rss.size() == 1) {
                                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(EventNotificationService.this)
                                        .setSmallIcon(R.drawable.ic_launcher)
                                        .setContentTitle("You have 1 new event!")
                                        .setContentText(rss.get(0).getName()).setAutoCancel(true);
                                mBuilder.setDefaults(Notification.DEFAULT_ALL);

                                Intent resultIntent = new Intent(EventNotificationService.this, FrontalActivity.class);
                                TaskStackBuilder stackBuilder = TaskStackBuilder.create(EventNotificationService.this);
                                stackBuilder.addParentStack(FrontalActivity.class);
                                stackBuilder.addNextIntent(resultIntent);
                                PendingIntent resultPendingIntent = PendingIntent.getActivity(EventNotificationService.this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                mBuilder.setContentIntent(resultPendingIntent);
                                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                mNotificationManager.notify(1339, mBuilder.build());

                            }
                        }


                        // Override local database and put in RSS events.
                        int i = 0;
                        dbHandler.deleteEvents(null, null);
                        while (i < rssEvents.length) {
                            EventInfoWrapper event = rssEvents[i];
                            try {
                                dbHandler.addEvent(event);
                            } catch (EventDatabaseHandler.EventCSVReadError e) {
                                e.printStackTrace();
                            }
                            i++;
                        }
                    }
                    myDB.close();

            }
        }).start();

        return Service.START_NOT_STICKY;
    }

    public IBinder onBind(Intent arg0) {
        Log.i(TAG, "Service onBind");
        return null;
    }

    @Override
    public void onDestroy() {

        isRunning = false;

        Log.i(TAG, "Service onDestroy");
    }
}

