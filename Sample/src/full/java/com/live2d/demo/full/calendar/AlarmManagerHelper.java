package com.live2d.demo.full.calendar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.util.Calendar;
import java.util.Map;

public class AlarmManagerHelper {

    // 알림 예약하는 정적 메서드
    public static void setAlarm(Context context, int requestCode, String title, long alarmTimeInMillis) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        SharedPreferences prefs = context.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("alarm_title_" + requestCode, title);
        editor.putLong("alarm_time_" + requestCode, alarmTimeInMillis);
        editor.putInt("alarm_request_code_" + requestCode, requestCode);
        editor.apply();
        try {
            if (alarmManager != null) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTimeInMillis,
                        pendingIntent
                );
                Log.d("AlarmManagerHelper", "알림 예약 시간: " + alarmTimeInMillis);
            }
        } catch (Exception e) {
            Log.e("AlarmManagerHelper", "알림 예약 실패: " + e.getMessage());
        }
    }

    public static void cancelFutureAlarms(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        SharedPreferences prefs = context.getSharedPreferences("MyAlarms", Context.MODE_PRIVATE);

        long now = System.currentTimeMillis();
        Map<String, ?> allPrefs = prefs.getAll();

        for (String key : allPrefs.keySet()) {
            if (key.startsWith("alarm_time_")) {
                long alarmTime = prefs.getLong(key, -1);
                if (alarmTime > now) {
                    String id = key.replace("alarm_time_", "");
                    int requestCode = prefs.getInt("alarm_request_code_" + id, -1);

                    if (requestCode != -1) {
                        Intent intent = new Intent(context, AlarmReceiver.class);
                        PendingIntent pi = PendingIntent.getBroadcast(
                                context,
                                requestCode,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                        );
                        if (pi != null && alarmManager != null) {
                            alarmManager.cancel(pi);
                            pi.cancel();
                            Log.d("AlarmCancel", "알람 취소 완료: requestCode=" + requestCode);
                        }
                    }
                }
            }
        }
    }
    public static void cancelAlarmByRequestCode(Context context, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (pi != null && alarmManager != null) {
            alarmManager.cancel(pi);
            pi.cancel();
            Log.d("AlarmManagerHelper", "알람 취소 완료: requestCode=" + requestCode);
        }
    }

}