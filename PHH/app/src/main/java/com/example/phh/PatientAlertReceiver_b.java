
package com.example.phh;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

//알람 시간이 되었을 때 동작할 기능
public class PatientAlertReceiver_b extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PatientNotificationHelper notificationHelper = new PatientNotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getChannelNotification(2);
        notificationHelper.getManager().notify(1,nb.build());

    }
}
