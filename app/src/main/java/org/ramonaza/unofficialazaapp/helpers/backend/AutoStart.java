package org.ramonaza.unofficialazaapp.helpers.backend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Yuval Zach aka kingi2001 on 1/1/2016.
 */
public class AutoStart extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        EventNotificationService.startRepeater(context);
    }
}
