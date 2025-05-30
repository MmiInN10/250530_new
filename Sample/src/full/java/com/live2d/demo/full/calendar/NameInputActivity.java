package com.live2d.demo.full.calendar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.live2d.demo.R;
import com.live2d.demo.full.MainActivity;

public class NameInputActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // userName이 이미 저장되어 있으면 바로 MainActivity로 이동
        if (!isFirstLaunch()) {
            startActivity(new Intent(NameInputActivity.this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_name_input);
        getSupportActionBar().hide();

        EditText etName = findViewById(R.id.etName);
        Button btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (!name.isEmpty()) {
                getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                        .edit()
                        .putString("userName", name)
                        .apply();

                Intent intent = new Intent(NameInputActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                showCustomToast("이름을 입력하세요.");
            }
        });
    }
    private boolean isFirstLaunch() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userName = prefs.getString("userName", null);
        return (userName == null || userName.isEmpty());
    }
}