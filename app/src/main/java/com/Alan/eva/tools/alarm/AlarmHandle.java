package com.Alan.eva.tools.alarm;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;

public class AlarmHandle {

    private final static String TAG = "wei";

    public AlarmHandle() {
    }

    // 增加一个闹钟
    public static void addAlarm(Context context, Alarm alarm) {

        ContentValues values = alarm2ContentValues(alarm);
        Uri uri = context.getContentResolver().insert(Alarm.Columns.CONTENT_URI, values);
        alarm.id = (int) ContentUris.parseId(uri);
        Log.v(TAG, "增加了一条闹钟");
    }

    // 删除一个闹钟
    public static void deleteAlarm(Context context, int alarmId) {
        Uri uri = ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarmId);
        context.getContentResolver().delete(uri, null, null);
        Log.v(TAG, "删除一个闹钟");
    }

    // 删除了所有闹钟
    public static void deleteAllAlarm(Context context) {
        context.getContentResolver().delete(Alarm.Columns.CONTENT_URI, null, null);
        Log.v(TAG, "删除了所有闹钟");
    }

    public static void updateAlarm(Context context, ContentValues values, int alarmId) {
        Uri uri = ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarmId);
        context.getContentResolver().update(uri, values, null, null);
        Log.v(TAG, "修改了一条闹钟");

    }

    // 根据ID号获得闹钟的信息
    public static Alarm getAlarm(Context context, int alarmId) {
        Uri uri = ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarmId);
        Cursor cursor = context.getContentResolver().query(uri, Alarm.Columns.ALARM_QUERY_COLUMNS, null, null, null);
        Alarm alarm = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                alarm = new Alarm(cursor);
            }
            cursor.close();
        }
        return alarm;
    }

    static Alarm getNextAlarm(Context context) {
        Cursor cursor = context.getContentResolver().query(Alarm.Columns.CONTENT_URI, Alarm.Columns.ALARM_QUERY_COLUMNS,
                Alarm.Columns.ENABLED_WHER, null, Alarm.Columns.ENABLED_SORT_ORDER);
        Alarm alarm = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                alarm = new Alarm(cursor);
            }
            cursor.close();
        }
        return alarm;
    }

    // 获得所有闹钟
    public static ArrayList<Alarm> getAlarms(Context context) {
        ArrayList<Alarm> alarmList = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(Alarm.Columns.CONTENT_URI, Alarm.Columns.ALARM_QUERY_COLUMNS,
                null, null, Alarm.Columns.DEFAULT_SORT_ORDER);
        if (cursor != null) {
            int count = cursor.getCount();
            if (count > 0) {
                while (cursor.moveToNext()) {
                    alarmList.add(new Alarm(cursor));
                }
                cursor.close();
            }
        }
        return alarmList;
    }

    private static ContentValues alarm2ContentValues(Alarm alarm) {
        ContentValues values = new ContentValues();
        values.put(Alarm.Columns.HOUR, alarm.hour);
        values.put(Alarm.Columns.MINUTES, alarm.minutes);
        values.put(Alarm.Columns.REPEAT, alarm.repeat);
        values.put(Alarm.Columns.BELL, alarm.bell);
        values.put(Alarm.Columns.VIBRATE, alarm.vibrate);
        values.put(Alarm.Columns.LABEL, alarm.label);
        values.put(Alarm.Columns.ENABLED, alarm.enabled);
        return values;
    }
}
