package org.ramonaza.unofficialazaapp.helpers.backend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.ramonaza.unofficialazaapp.events.backend.EventNotificationService;
import org.ramonaza.unofficialazaapp.events.backend.EventUpdateService;

/**
 * Created by Yuval Zach aka kingi2001 on 1/1/2016.
 */
public class AutoStart extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        EventUpdateService.startRepeater(context);
        EventNotificationService.setUpNotifications(context);
    }
}
