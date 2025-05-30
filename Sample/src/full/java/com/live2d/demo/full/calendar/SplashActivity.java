package com.live2d.demo.full.calendar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.live2d.demo.R;
import com.live2d.demo.full.MainActivity;

import java.util.Map;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 스플래시 테마 적용
        setTheme(R.style.Theme_Demo_Splash);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        boolean isFirstLaunch = isFirstLaunch();
        Log.d("SplashActivity", "isFirstLaunch=" + isFirstLaunch());

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isFirstLaunch()) {
                    // 최초 실행 시 이름 입력 화면으로 이동
                    startActivity(new Intent(SplashActivity.this, NameInputActivity.class));
                } else {
                    // 이후 실행 시 메인 화면으로 이동
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                }
                finish(); // 스플래시 액티비티 종료
            }
        }, 1500); // 1.5초 대기 후 실행
        logAllPrefs();

    }

    private boolean isFirstLaunch() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        boolean isFirst = prefs.getBoolean("isFirstLaunch", true);
        Log.d("SplashActivity", "isFirstLaunch() 불린값: " + isFirst);

        return true;
    }
    private void logAllPrefs() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("SharedPrefsDump", entry.getKey() + " = " + entry.getValue());
        }
    }


    // 리셋 함수: 버튼 클릭 시 호출되어 SharedPreferences 초기화
//    private void resetFirstLaunch() {
//        getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
//                .edit()
//                .putBoolean("isFirstLaunch", true)  // 최초 실행 상태로 설정
//                .apply();
//    }
}