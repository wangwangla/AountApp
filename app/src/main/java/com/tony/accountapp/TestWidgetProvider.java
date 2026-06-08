package com.tony.accountapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Random;

public class TestWidgetProvider extends AppWidgetProvider {

    private static final String ACTION_WIDGET_TICK = "com.tony.accountapp.action.WIDGET_TICK";
    private static final long UPDATE_INTERVAL_MS = 1000L;
    private static final Random RANDOM = new Random();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        updateWidgets(context);
        scheduleUpdates(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        updateWidgets(context);
        scheduleUpdates(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        cancelUpdates(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_WIDGET_TICK.equals(intent.getAction())) {
            updateWidgets(context);
            scheduleUpdates(context);
        }
    }

    private void updateWidgets(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName provider = new ComponentName(context, TestWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(provider);
        Log.d("TestWidgetProvider", "Updating widgets: " + ids.length + " instances");
        for (int id : ids) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.test_widget);
            views.setTextViewText(R.id.widget_text, String.valueOf(RANDOM.nextInt(1000)));
            manager.updateAppWidget(id, views);
        }
    }

    private void scheduleUpdates(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        long triggerAt = SystemClock.elapsedRealtime() + UPDATE_INTERVAL_MS;
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, getTickPendingIntent(context));
    }

    private void cancelUpdates(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        alarmManager.cancel(getTickPendingIntent(context));
    }

    private PendingIntent getTickPendingIntent(Context context) {
        Intent intent = new Intent(context, TestWidgetProvider.class);
        intent.setAction(ACTION_WIDGET_TICK);
        return PendingIntent.getBroadcast(
                context,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
