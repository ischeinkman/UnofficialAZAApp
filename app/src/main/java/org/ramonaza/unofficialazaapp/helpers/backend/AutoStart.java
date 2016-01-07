package org.ramonaza.unofficialazaapp.helpers.backend;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Yuval Zach aka kingi2001 on 1/1/2016.
 */
public class AutoStart extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
           AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            Intent i = new Intent(context, EventNotificationService.class);
            PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
            mgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, AlarmManager.INTERVAL_HALF_HOUR, AlarmManager.INTERVAL_HALF_HOUR, pi);
    }
}
