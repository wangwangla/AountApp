package com.tony.accountapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.RemoteViews;

public class TestWidgetProvider extends AppWidgetProvider {

    private static final String ACTION_WIDGET_TICK = "com.tony.accountapp.action.WIDGET_TICK";
    private static final long UPDATE_INTERVAL_MS = 1000L;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
        scheduleUpdates(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
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
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName provider = new ComponentName(context, TestWidgetProvider.class);
            int[] ids = manager.getAppWidgetIds(provider);
            for (int id : ids) {
                updateWidget(context, manager, id);
            }
            scheduleUpdates(context);
        }
    }

    private void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.test_widget);
        
        // 从 SharedPreferences 读取配置
        String text = loadWidgetText(context, widgetId);
        int backgroundColor = loadWidgetColor(context, widgetId);

        views.setTextViewText(R.id.widget_text, text);
        views.setInt(R.id.widget_background, "setBackgroundColor", backgroundColor);

        // 添加点击事件，点击打开配置页
        Intent configIntent = new Intent(context, WidgetConfigActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        configIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, widgetId,
                configIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_background, configPendingIntent);

        manager.updateAppWidget(widgetId, views);
    }

    private String loadWidgetText(Context context, int widgetId) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE);
        return prefs.getString("widget_" + widgetId + "_text", "Widget Text");
    }

    private int loadWidgetColor(Context context, int widgetId) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE);
        return prefs.getInt("widget_" + widgetId + "_color", 0xFFFFFFFF);
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
