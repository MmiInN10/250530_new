package com.live2d.demo.full.calendar;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.live2d.demo.R;

public class BaseActivity extends AppCompatActivity {
    // 공통 커스텀 토스트 함수
    protected void showCustomToast(String message) {
        View customToastView = LayoutInflater.from(this).inflate(R.layout.custom_toast, null);

        TextView textView = customToastView.findViewById(R.id.toast_text);
        textView.setText(message);

        Toast toast = new Toast(this);
        toast.setView(customToastView);

        toast.setGravity(Gravity.BOTTOM, 0, 200);
        toast.setDuration(Toast.LENGTH_SHORT);

        toast.show();
    }
}
