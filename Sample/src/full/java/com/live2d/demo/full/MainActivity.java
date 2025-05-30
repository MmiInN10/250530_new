package com.live2d.demo.full;
/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
// import 추가
import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.app.PendingIntent;
import android.content.Intent;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.content.Intent;
import android.widget.Button;
import android.widget.FrameLayout;
import android.view.Gravity;
import android.app.AlarmManager;
import android.content.Intent;
import android.os.Process;
import android.content.Context;
import com.live2d.demo.R;
import com.live2d.demo.full.GLRenderer;
import com.live2d.demo.full.LAppDelegate;
import com.live2d.demo.full.calendar.SettingActivity;
import com.live2d.demo.full.calendar.CalendarActivity;
import com.jakewharton.processphoenix.ProcessPhoenix;
import java.util.Locale;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.InputStream;
import java.io.IOException;
import android.view.ScaleGestureDetector;
import android.util.Log;
import androidx.core.app.ActivityCompat;

public class MainActivity extends Activity {
    private GLSurfaceView glSurfaceView;
    private ImageView accessoryView;

    private GLRenderer glRenderer;

    // 🎙️ 추가되는 부분
    private MediaRecorder mediaRecorder;
    private TextToSpeech textToSpeech;
    private String audioFilePath;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final String OPENAI_API_KEY = "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";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
        setContentView(R.layout.activity_main);

        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2);

        glRenderer = new GLRenderer();
        glSurfaceView.setRenderer(glRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final float pointX = event.getX();
                final float pointY = event.getY();

                glSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                LAppDelegate.getInstance().onTouchBegan(pointX, pointY);
                                break;
                            case MotionEvent.ACTION_UP:
                                LAppDelegate.getInstance().onTouchEnd(pointX, pointY);
                                break;
                            case MotionEvent.ACTION_MOVE:
                                LAppDelegate.getInstance().onTouchMoved(pointX, pointY);
                                break;
                        }
                    }
                });
                return true;  // 여기서 true 반환해서 터치 이벤트를 소비하게 만든다
            }
        });
        // [1] 먼저 악세서리용 ImageView 생성
        accessoryView = new ImageView(this);
        accessoryView.setVisibility(View.INVISIBLE);

        FrameLayout.LayoutParams accessoryParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        accessoryParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        accessoryParams.topMargin = 200;
        accessoryView.setLayoutParams(accessoryParams);


// [3] 이제 터치 리스너 추가
        accessoryView.setOnTouchListener(new View.OnTouchListener() {
            private float offsetX, offsetY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        offsetX = event.getRawX() - v.getX();
                        offsetY = event.getRawY() - v.getY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        v.setX(event.getRawX() - offsetX);
                        v.setY(event.getRawY() - offsetY);
                        return true;
                }
                return false;
            }
        });





// 1. 악세서리용 ImageView 생성
        accessoryView.setVisibility(View.INVISIBLE); // 처음엔 숨겨둔다
// 이미 선언된 accessoryParams를 다시 설정
        accessoryParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        accessoryParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        accessoryParams.topMargin = 200;
        accessoryView.setLayoutParams(accessoryParams);
// 2. "악세서리 선택" 버튼 생성
        Button accessoryMenuButton = new Button(this);
        accessoryMenuButton.setText("악세서리 선택");

        ConstraintLayout.LayoutParams menuButtonParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        menuButtonParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        menuButtonParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        menuButtonParams.setMargins(0, 0, 50, 320);

        accessoryMenuButton.setLayoutParams(menuButtonParams);

// 3. "악세서리 제거" 버튼 생성
        Button removeAccessoryButton = new Button(this);
        removeAccessoryButton.setText("악세서리 제거");

        ConstraintLayout.LayoutParams removeButtonParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        removeButtonParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        removeButtonParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        removeButtonParams.setMargins(0, 0, 50, 220);

        removeAccessoryButton.setLayoutParams(removeButtonParams);

        removeAccessoryButton.setOnClickListener(v -> {
            accessoryView.setVisibility(View.INVISIBLE);
        });
        // 4. 버튼 동작 설정
        accessoryMenuButton.setOnClickListener(v -> {
            String[] accessories = {"모자", "안경", "리본"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("악세서리 선택")
                    .setItems(accessories, (dialog, which) -> {
                        switch (which) {
                            case 0: // 모자
                                loadAccessoryFromAssets(accessoryView, "hat.png");
                                accessoryView.setVisibility(View.VISIBLE);
                                break;
                            case 1: // 안경
                                loadAccessoryFromAssets(accessoryView, "glasses.png");
                                accessoryView.setVisibility(View.VISIBLE);
                                break;
                            case 2: // 리본
                                loadAccessoryFromAssets(accessoryView, "ribbon.png");
                                accessoryView.setVisibility(View.VISIBLE);
                                break;
                        }
                    });
            builder.show();
        });
        // --- [1] accessoryView 초기화 ---
        accessoryView = new ImageView(this);
        accessoryView.setVisibility(View.INVISIBLE);

// 크기 기본 설정 (초기 크기 1배)
        accessoryView.setScaleX(1.0f);
        accessoryView.setScaleY(1.0f);

// 레이아웃 파라미터 설정
        accessoryParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        accessoryParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        accessoryParams.topMargin = 200;
        accessoryView.setLayoutParams(accessoryParams);


// --- [3] ScaleGestureDetector 준비 ---
        ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();
                accessoryView.setScaleX(accessoryView.getScaleX() * scaleFactor);
                accessoryView.setScaleY(accessoryView.getScaleY() * scaleFactor);
                return true;
            }
        });


// --- [4] accessoryView 터치 리스너 연결 (이동 + 크기조절) ---
        accessoryView.setOnTouchListener(new View.OnTouchListener() {
            private float offsetX, offsetY;
            private float initialAngle = 0;
            private boolean isRotating = false;

            private float getAngle(MotionEvent event) {
                double deltaX = (event.getX(1) - event.getX(0));
                double deltaY = (event.getY(1) - event.getY(0));
                double radians = Math.atan2(deltaY, deltaX);
                return (float) Math.toDegrees(radians);
            }

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scaleGestureDetector.onTouchEvent(event);  // 핀치로 크기조정 먼저 감지

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        offsetX = event.getRawX() - v.getX();
                        offsetY = event.getRawY() - v.getY();
                        return true;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        if (event.getPointerCount() == 2) {
                            initialAngle = getAngle(event);
                            isRotating = true;
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        if (event.getPointerCount() == 1 && !isRotating) {
                            // 한 손가락 드래그 이동
                            v.setX(event.getRawX() - offsetX);
                            v.setY(event.getRawY() - offsetY);
                        }
                        if (event.getPointerCount() == 2 && isRotating) {
                            // 두 손가락 회전
                            float currentAngle = getAngle(event);
                            float rotation = v.getRotation() + (currentAngle - initialAngle);
                            v.setRotation(rotation);
                            initialAngle = currentAngle;
                        }
                        constrainAccessoryInsideScreen(v);
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        isRotating = false;
                        return true;
                }

                return false;
            }

        });


        // 🆕 "말하기" 버튼
        Button recordButton = new Button(this);
        recordButton.setText("말하기");
        ConstraintLayout.LayoutParams recordButtonParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        recordButtonParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        recordButtonParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        recordButtonParams.topMargin = 100;
        recordButtonParams.setMarginStart(100);
        recordButton.setOnClickListener(v -> {
            if (mediaRecorder == null) {
                startRecording();
            } else {
                stopRecording();
                transcribeAudio();
            }
        });
        setContentView(R.layout.activity_main);
        ConstraintLayout rootLayout = findViewById(R.id.root_layout);

        rootLayout.addView(glSurfaceView,0);
        rootLayout.addView(accessoryView);
        rootLayout.addView(accessoryMenuButton);
        rootLayout.addView(removeAccessoryButton);
        rootLayout.addView(recordButton);
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(Locale.KOREAN);  // 한국어
                }
            }
        });

        LAppDelegate.getInstance().onStart(this);

        audioFilePath = getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/recorded_audio.mp4";

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
        LAppDelegate.getInstance().onStart(this);

        ImageButton buttonMain = findViewById(R.id.button_main); // 홈 버튼 추가
        ImageButton buttonCalendar = findViewById(R.id.button_calendar);
        ImageButton buttonSetting = findViewById(R.id.button_setting);


        buttonMain.setOnClickListener(v -> {
            ProcessPhoenix.triggerRebirth(MainActivity.this);
        });


        buttonCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
            startActivity(intent);
            finish();
        });

        buttonSetting.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);
            finish();
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

    }
    private void loadAccessoryFromAssets(ImageView view, String fileName) {
        try {
            InputStream inputStream = getAssets().open(fileName);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            view.setImageBitmap(bitmap);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void constrainAccessoryInsideScreen(View v) {
        float x = v.getX();
        float y = v.getY();
        float width = v.getWidth() * v.getScaleX();
        float height = v.getHeight() * v.getScaleY();

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        // 왼쪽, 위쪽 경계 체크
        if (x < 0) x = 0;
        if (y < 0) y = 0;

        // 오른쪽, 아래쪽 경계 체크
        if (x + width > screenWidth) x = screenWidth - width;
        if (y + height > screenHeight) y = screenHeight - height;

        v.setX(x);
        v.setY(y);
    }

    private void startRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.release();  // 이미 있으면 먼저 해제
            mediaRecorder = null;
        }
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(audioFilePath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }


    private void transcribeAudio() {
        executorService.execute(() -> {
            try {
                File audioFile = new File(audioFilePath);
                if (!audioFile.exists()) return;

                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", audioFile.getName(),
                                RequestBody.create(audioFile, MediaType.parse("audio/mp4")))
                        .addFormDataPart("model", "whisper-1")
                        .addFormDataPart("language", "ko")
                        .build();

                Request request = new Request.Builder()
                        .url("https://api.openai.com/v1/audio/transcriptions")
                        .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String result = new JSONObject(response.body().string()).getString("text");
                    sendMessageToGPT(result);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void sendMessageToGPT(String userInput) {
        executorService.execute(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");

                JSONArray messages = new JSONArray();
                messages.put(new JSONObject()
                        .put("role", "system")
                        .put("content", "너는 한국어 AI 친구야. 친근하게 짧게 대화해줘."));
                messages.put(new JSONObject()
                        .put("role", "user")
                        .put("content", userInput));

                JSONObject json = new JSONObject();
                json.put("model", "gpt-3.5-turbo");
                json.put("messages", messages);

                RequestBody body = RequestBody.create(json.toString(), JSON);
                Request request = new Request.Builder()
                        .url(OPENAI_API_URL)
                        .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                        .addHeader("Content-Type", "application/json")
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String botReply = new JSONObject(response.body().string())
                            .getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");

                    analyzeEmotion(botReply);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void analyzeEmotion(String botReply) {
        executorService.execute(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");

                JSONArray messages = new JSONArray();
                messages.put(new JSONObject()
                        .put("role", "system")
                        .put("content", "다음 문장을 읽고 '기쁨', '슬픔', '화남', '평온' 중 하나로만 답해주세요."));
                messages.put(new JSONObject()
                        .put("role", "user")
                        .put("content", botReply));

                JSONObject json = new JSONObject();
                json.put("model", "gpt-3.5-turbo");
                json.put("messages", messages);

                RequestBody body = RequestBody.create(json.toString(), JSON);
                Request request = new Request.Builder()
                        .url(OPENAI_API_URL)
                        .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                        .addHeader("Content-Type", "application/json")
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String emotion = new JSONObject(response.body().string())
                            .getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                            .trim();

                    runOnUiThread(() -> updateCharacterEmotion(emotion));
                    runOnUiThread(() -> {
                        if (textToSpeech != null) {
                            textToSpeech.speak(botReply, TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void updateCharacterEmotion(String emotion) {
        try {
            if (LAppDelegate.getInstance() != null
                    && LAppDelegate.getInstance().getLive2DManager() != null
                    && LAppDelegate.getInstance().getLive2DManager().getModel(0) != null) {
                String motionName = "motion1";  // 기본 모션

                if (emotion.contains("기쁨")) {
                    motionName = "motion3";
                } else if (emotion.contains("슬픔")) {
                    motionName = "motion2";
                } else if (emotion.contains("화남")) {
                    motionName = "motion5";
                } else if (emotion.contains("평온")) {
                    LAppDelegate.getInstance().getLive2DManager().getModel(0).setExpression("neutral");
                }
                // 모션 실행
                LAppDelegate.getInstance().getLive2DManager().getModel(0)
                        .startMotion("TapBody", getMotionIndex(motionName), 1);
                Log.d("Live2D", "모션 실행: " + motionName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 모션 인덱스 얻기
    private int getMotionIndex(String motionName) {
        switch (motionName) {
            case "motion1": return 0;
            case "motion2": return 1;
            case "motion3": return 2;
            case "motion4": return 3;
            default: return 0;
        }
    }
    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}

