package com.live2d.demo.full.calendar;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.SignInButton;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.jakewharton.processphoenix.ProcessPhoenix;
import com.live2d.demo.R;
import com.live2d.demo.full.MainActivity;
import java.util.Collections;
import android.widget.Button;
import com.live2d.demo.full.calendar.AlarmManagerHelper;


public class SettingActivity extends BaseActivity {
    private static final int REQUEST_ACCOUNT_PICKER = 1001;
    private GoogleAccountCredential mCredential;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getSupportActionBar().hide();

        // 하단 버튼 설정
        ImageButton buttonMain = findViewById(R.id.button_main);
        ImageButton buttonCalendar = findViewById(R.id.button_calendar);
        ImageButton buttonSetting = findViewById(R.id.button_setting);

        buttonMain.setOnClickListener(v -> {
            ProcessPhoenix.triggerRebirth(SettingActivity.this);
        });

        buttonCalendar.setOnClickListener(v -> {
            startActivity(new Intent(this, CalendarActivity.class));
            finish();
        });

        buttonSetting.setOnClickListener(v -> {
        });

        // 사용자 이름 표시
        Context context = this;
        String userName = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                .getString("userName", "사용자");

        TextView greetingTextView = findViewById(R.id.textViewGreeting);
        greetingTextView.setText(userName + "님, 안녕하세요!");

        ImageView editIcon = findViewById(R.id.imageViewEdit);
        editIcon.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("이름 수정");

            EditText input = new EditText(context);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setText(userName);
            builder.setView(input);

            builder.setPositiveButton("확인", (dialog, which) -> {
                String newName = input.getText().toString();
                greetingTextView.setText(newName + "님, 안녕하세요!");
                getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                        .edit().putString("userName", newName).apply();
            });

            builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());
            builder.show();
        });
        // Google 인증 초기화
        mCredential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singletonList(CalendarScopes.CALENDAR_READONLY)
        ).setBackOff(new ExponentialBackOff());

        SignInButton signInButton = findViewById(R.id.btnGoogleSignIn);
        signInButton.setOnClickListener(v -> signInWithGoogle());

        Button buttonCancelNotifications = findViewById(R.id.button_cancel_notifications);
        buttonCancelNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SettingActivity.this)
                        .setTitle("알람 해제")
                        .setMessage("전체 알람을 해제하시겠습니까?")
                        .setPositiveButton("예", (dialog, which) -> {
                            AlarmManagerHelper.cancelAllAlarms(SettingActivity.this);
                            showCustomToast("알림 해제 완료");
                        })
                        .setNegativeButton("아니요", null)
                        .show();
            }
        });
        buttonCancelNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SettingActivity.this)
                        .setTitle("알람 해제")
                        .setMessage("전체 알람을 해제하시겠습니까?")
                        .setPositiveButton("예", (dialog, which) -> {
                            // 푸시 알림 전체 취소
                            AlarmManagerHelper.cancelAllAlarms(SettingActivity.this);

                            // 챗봇 알림도 함께 취소
                            AlarmManagerHelper.ChatbotAlarmHelper.cancelChatbotAlarm(SettingActivity.this);

                            showCustomToast("알림 해제 완료");
                        })
                        .setNegativeButton("아니요", null)
                        .show();
            }
        });

    }

    private void signInWithGoogle() {
        startActivityForResult(
                mCredential.newChooseAccountIntent(),
                REQUEST_ACCOUNT_PICKER
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode == RESULT_OK && data != null) {
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);

                PreferenceManager.getDefaultSharedPreferences(this)
                        .edit().putString("accountName", accountName).apply();
                showCustomToast("계정 연동 완료");

            }
        }
    }

}