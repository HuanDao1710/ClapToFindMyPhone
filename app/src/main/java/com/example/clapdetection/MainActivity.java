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

import com.example.clapdetection.service.ClapDetectionService;

public class MainActivity extends AppCompatActivity {

    final int RECORD_AUDIO = 0;

    private boolean isActive = false;
    Button roundButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO);
        }

        roundButton = findViewById(R.id.roundButton);
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