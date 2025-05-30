package com.live2d.demo.full.calendar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class AlarmManagerHelper {

    // 알림 예약하는 정적 메서드
    public static void setAlarm(Context context, int requestCode, String title, long alarmTimeInMillis) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        // PendingIntent로 AlarmReceiver를 트리거
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        SharedPreferences prefs = context.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("alarm_title_" + requestCode, title).apply();

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
    public static class ChatbotAlarmHelper {
        private static final int REQUEST_CODE = 2001; // 기존 푸시 알림과 다른 코드

        public static void setChatbotAlarm(Context context, long alarmTimeInMillis) {
            Intent intent = new Intent(context, ChatbotAlarmReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (alarmManager != null) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTimeInMillis,
                        pendingIntent
                );
                Log.d("ChatbotAlarmHelper", "챗봇 알림 예약됨: " + alarmTimeInMillis);
            }
        }
        public static void cancelChatbotAlarm(Context context) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            Intent intent = new Intent(context, ChatbotAlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    REQUEST_CODE, // 2001
                    intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );

            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
                Log.d("ChatbotAlarmHelper", "챗봇 알림 취소 완료");
            } else {
                Log.d("ChatbotAlarmHelper", "챗봇 알림 취소할 PendingIntent 없음");
            }
        }
    }
    public static void cancelAllAlarms(Context context) {
        // AlarmManager 가져오기
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e("AlarmManagerHelper", "AlarmManager is null!");
            return;
        }

        // 저장된 알람 제목 정보를 불러오기 위한 SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE);

        // 예약된 알람을 최대 100개까지 확인 (requestCode: 0~99)
        for (int requestCode = 0; requestCode < 100; requestCode++) {
            String title = prefs.getString("alarm_title_" + requestCode, null);

            // 로그: 현재 확인 중인 requestCode와 저장된 title
            Log.d("AlarmManagerHelper", "검사 중인 requestCode=" + requestCode + ", title=" + title);

            // title이 없으면 알람이 없다는 뜻 → 넘어감
            if (title == null) continue;

            // 알람을 받기 위한 인텐트 생성 (AlarmReceiver 대상)
            Intent intent = new Intent(context, AlarmReceiver.class);

            // 기존 PendingIntent를 찾기 (FLAG_NO_CREATE 사용)
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );

            if (pendingIntent != null) {
                // 알람 매니저로 알람 취소
                alarmManager.cancel(pendingIntent);
                // PendingIntent도 취소
                pendingIntent.cancel();

                // SharedPreferences에서도 삭제
                prefs.edit().remove("alarm_title_" + requestCode).apply();

                // 로그: 알람 취소 완료
                Log.d("AlarmManagerHelper", "알람 취소 완료 requestCode=" + requestCode);
            } else {
                // 로그: PendingIntent가 없음
                Log.d("AlarmManagerHelper", "취소할 PendingIntent 없음: requestCode=" + requestCode);
            }
        }
    }

}
