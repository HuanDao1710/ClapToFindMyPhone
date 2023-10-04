package com.example.clapdetection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.example.clapdetection.service.ClapDetectionService;

public class MainActivity extends AppCompatActivity {

    final int RECORD_AUDIO = 0;

    private boolean isActive = ClapDetectionService.isInstanceCreated();
    Button roundButton;
    SeekBar sensitivitySeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        roundButton = findViewById(R.id.roundButton);
        if(isActive) {
            roundButton.setText("Active");
        } else {
            roundButton.setText("Inactive");
        }
        sensitivitySeekBar = findViewById(R.id.sensitivitySeekBar);
        sensitivitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Xử lý sự kiện khi người dùng bắt đầu di chuyển thanh SeekBar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Xử lý sự kiện khi người dùng kết thúc di chuyển thanh SeekBar
                ClapDetectionService.setSensitivity((double) seekBar.getProgress());
            }
        });

        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO);
        }

        roundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Xử lý sự kiện nhấn nút
                if (!isActive) {
                    isActive = true;
                    roundButton.setText("Active");
                    Intent serviceIntent = new Intent(MainActivity.this, ClapDetectionService.class);
                    MainActivity.this.startService(serviceIntent);
                } else {
                    isActive = false;
                    roundButton.setText("Inactive");
                    Intent serviceIntent = new Intent(MainActivity.this, ClapDetectionService.class);
                    MainActivity.this.stopService(serviceIntent);
                }
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}