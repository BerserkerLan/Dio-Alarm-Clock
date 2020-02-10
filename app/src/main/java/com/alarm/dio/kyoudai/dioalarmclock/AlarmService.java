package com.alarm.dio.kyoudai.dioalarmclock;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class AlarmService extends IntentService {

    private NotificationManager alarmNotificationManager;

    public AlarmService() {
        super("Dio Alarm Service");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        showAlarmAndNotification();
    }

    public void showAlarmAndNotification() {
        Log.d("AlarmService", "Preparing to send notification...: ");
        alarmNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, AlarmPlayingActivity.class), 0);

        NotificationCompat.Builder alamNotificationBuilder = new NotificationCompat.Builder(
                this).setContentTitle("Alarm").setSmallIcon(R.drawable.ic_alarm_status_icon)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Dio commands you, WAKE UP"))
                .setContentText("KHONO DIO DA");


        alamNotificationBuilder.setContentIntent(contentIntent);
        alarmNotificationManager.notify(1, alamNotificationBuilder.build());
        Log.d("AlarmService", "Notification sent.");
    }
}
