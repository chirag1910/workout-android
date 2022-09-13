package com.sugarsnooper.workout;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class ReminderBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("workout", Context.MODE_PRIVATE);
        boolean allowNotifications = sharedPreferences.getBoolean("notificationSettings", true);
        if (allowNotifications) {

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.app_notification_collapsed);

            Intent notificationTargetIntent = new Intent(context, SetExercise.class);
            notificationTargetIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 200, notificationTargetIntent, PendingIntent.FLAG_UPDATE_CURRENT);

//            Intent notificationActionButtonIntent = new Intent(context, SetExercise.class);
//            PendingIntent pendingActionButtonIntent = PendingIntent.getBroadcast(context,
//                    200, notificationActionButtonIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new NotificationCompat.Builder(context, "notifyUser")
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentIntent(pendingIntent)
                    .setCustomContentView(remoteViews)
                    .setAutoCancel(true)
                    .build();

            notificationManager.notify(200, notification);

            setUpNotification(context);
        }
    }

    public void setUpNotification(Context context){
        if (Globals.allowNotifications) {
            Calendar calendar = Calendar.getInstance();

            calendar.set(Calendar.HOUR_OF_DAY, Globals.notificationTimeHour);
            calendar.set(Calendar.MINUTE, Globals.notificationTimeMinute);
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getTime().getDate() + 1);

            Intent intent = new Intent(context.getApplicationContext(), ReminderBroadcast.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 200, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }
}
