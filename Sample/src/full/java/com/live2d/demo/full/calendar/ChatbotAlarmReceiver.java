package com.live2d.demo.full.calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.live2d.demo.full.MainActivity;

public class ChatbotAlarmReceiver extends BroadcastReceiver {

    private static final String PREFS_NAME = "MyPrefs";  // SharedPreferences 파일명
    private static final String KEY_SWITCH_CHATBOT_ALARM = "switchChatbotAlarm"; // 키 이름

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isChatbotAlarmOn = prefs.getBoolean(KEY_SWITCH_CHATBOT_ALARM, false);

        if (!isChatbotAlarmOn) {
            return;
        }
            Log.d("ChatbotAlarmReceiver", "챗봇 알림: MainActivity로 이동");

            Intent mainIntent = new Intent(context, MainActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(mainIntent);
        }
}