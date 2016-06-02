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
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.events.ui.activities.EventPageActivity;
import org.ramonaza.unofficialazaapp.frontpage.ui.activities.FrontalActivity;
import org.ramonaza.unofficialazaapp.helpers.backend.DatabaseHandler;
import org.ramonaza.unofficialazaapp.helpers.backend.PreferenceHelper;
import org.ramonaza.unofficialazaapp.people.backend.EventDatabaseHandler;
import org.ramonazaapi.events.EventInfoWrapper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by ilan on 1/11/16.
 */
public class EventNotificationService extends Service {

    public static final String EVENT_DB_ID = "org.ramonaza.unofficialazaapp.eventdbid";
    private static final long TIME_MULTIPLIER = 1000 * 60;
    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

    public static void setUpNotifications(Context context) {
        boolean weInited = false;
        if (!DatabaseHandler.isInitialized()){
            DatabaseHandler.init(context);
            weInited = true;
        }
        long notifiyBeforeEvent = PreferenceHelper.getPreferences(context).getNotifyBeforeTime() * TIME_MULTIPLIER;
        if (notifiyBeforeEvent < 0l) {
            cancelNotifications(context);
            return;
        }
        EventDatabaseHandler dbHandler = (EventDatabaseHandler) DatabaseHandler.getHandler(EventDatabaseHandler.class);
        EventInfoWrapper[] allEvents = dbHandler.getEvents(null, null);
        AlarmManager mgr = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Date current = Calendar.getInstance().getTime();
        for (EventInfoWrapper event : allEvents) {
            DateFormat df = new SimpleDateFormat("EEEE, MMMM dd yyyy");
            Date eventDate;
            try {
                eventDate = df.parse(event.getDate());
            } catch (ParseException e) {
                continue;
            }
            if (eventDate.before(current)) continue;
            eventDate.setTime(eventDate.getTime() - notifiyBeforeEvent);
            Intent notificationIntent = new Intent(context, EventNotificationService.class);
            notificationIntent.putExtra(EVENT_DB_ID, event.getId());
            PendingIntent pendingNotifIntent = PendingIntent.getService(context, 0, notificationIntent, 0);
            mgr.set(AlarmManager.RTC, eventDate.getTime(), pendingNotifIntent);
        }
    }

    public static void cancelNotifications(Context context) {
        boolean weInited = false;
        if (!DatabaseHandler.isInitialized()){
            DatabaseHandler.init(context);
            weInited = true;
        }
        EventDatabaseHandler dbHandler = (EventDatabaseHandler) DatabaseHandler.getHandler(EventDatabaseHandler.class);
        EventInfoWrapper[] allEvents = dbHandler.getEvents(null, null);
        AlarmManager mgr = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Date current = Calendar.getInstance().getTime();
        for (EventInfoWrapper event : allEvents) {
            DateFormat df = new SimpleDateFormat("EEEE, MMMM dd yyyy");
            Date eventDate;
            try {
                eventDate = df.parse(event.getDate());
            } catch (ParseException e) {
                continue;
            }
            if (eventDate.before(current)) continue;
            Intent notificationIntent = new Intent(context, EventNotificationService.class);
            notificationIntent.putExtra(EVENT_DB_ID, event.getId());
            PendingIntent pendingNotifIntent = PendingIntent.getService(context, 0, notificationIntent, 0);
            mgr.cancel(pendingNotifIntent);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean weInited = false;
        if (!DatabaseHandler.isInitialized()){
            DatabaseHandler.init(this);
            weInited = true;
        }
        int eventId = intent.getIntExtra(EVENT_DB_ID, -1);

        //ID was not passed correctly
        if (eventId == -1) {
            stopSelf();
            return START_NOT_STICKY;
        }

        //Event with that ID exists in the db at all
        EventDatabaseHandler dbHandler = (EventDatabaseHandler) DatabaseHandler.getHandler(EventDatabaseHandler.class);
        EventInfoWrapper event = dbHandler.getEvent(eventId);
        if (event == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        //Event has valid time
        DateFormat df = new SimpleDateFormat("EEEE, MMMM dd yyyy");
        Date eventDate;
        try {
            eventDate = df.parse(event.getDate());
        } catch (ParseException e) {
            e.printStackTrace();
            stopSelf();
            return START_NOT_STICKY;
        }

        //Make sure we are using the correct time
        long notifyBeforeEvent = PreferenceHelper.getPreferences(this).getNotifyBeforeTime();
        if (!(Calendar.getInstance().getTimeInMillis() >= eventDate.getTime() - notifyBeforeEvent)) {
            stopSelf();
            return START_NOT_STICKY;
        }

        notifyEvent(event);
        stopSelf();
        return START_NOT_STICKY;
    }

    private void notifyEvent(EventInfoWrapper event) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        DateFormat df = new SimpleDateFormat("EEEE, MMMM dd yyyy");
        Date eventDate;
        try {
            eventDate = df.parse(event.getDate());
        } catch (ParseException e) {
            return;
        }
        DateFormat timeDf = new SimpleDateFormat("hh:mm aa");
        String title = String.format("%s starts at %s!", event.getName(), timeDf.format(eventDate));
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        mBuilder.setContentTitle(title)
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(largeIcon)
                .setContentText("Tap to see details.").setAutoCancel(true)
                .setProgress(0, 0, false);
        mBuilder.setDefaults(Notification.DEFAULT_ALL);

        Intent resultIntent = new Intent(this, EventPageActivity.class);
        resultIntent.putExtra(EventPageActivity.EVENT_DATA, event.getId());
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(FrontalActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager.notify(119955 + event.getId(), mBuilder.build());
    }
}
