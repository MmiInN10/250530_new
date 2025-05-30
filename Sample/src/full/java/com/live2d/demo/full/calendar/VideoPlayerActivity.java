package com.live2d.demo.full.calendar;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

import com.live2d.demo.R;
import com.live2d.demo.full.MainActivity;

public class VideoPlayerActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        getSupportActionBar().hide();

        VideoView videoView = findViewById(R.id.videoView);

        // 예: res/raw/notification_video.mp4 재생
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification_video);
        videoView.setVideoURI(videoUri);

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // 영상이 끝나면 MainActivity로 이동
                Intent mainIntent = new Intent(VideoPlayerActivity.this, MainActivity.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
                finish();
            }
        });

        videoView.start();
    }
}
