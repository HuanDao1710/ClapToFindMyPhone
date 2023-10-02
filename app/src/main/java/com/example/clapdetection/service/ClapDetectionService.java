package com.example.clapdetection.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.onsets.OnsetHandler;
import be.tarsos.dsp.onsets.PercussionOnsetDetector;

import com.example.clapdetection.R;
import com.example.clapdetection.TimeClapEntity;

import java.io.IOException;

public class ClapDetectionService extends Service {
    MediaPlayer mediaPlayer;
    MediaRecorder recorder;
    long clapDetectedNumber = 0;
    private int finishAmplitude;
    private final int amplitudeThreshold = 18000;
    private final int delayMillis = 200;
    Handler handler;
    private Runnable periodicUpdate ;

    private static final String NOTIFICATION_CHANEL_ID = "ClapDetectionChanel";
    private static final  int NOTIFICATION_ID = 1;
    NotificationCompat.Builder builder;

    TimeClapEntity timeClapEntity;
    private static final int SAMPLE_RATE = 44100;
    private static final int BUFFER_SIZE = 1024;
    private AudioDispatcher audioDispatcher;



    @Override
    public void onCreate() {
        super.onCreate();
        // ring
        mediaPlayer = MediaPlayer.create(this, R.raw.ring2 );
        mediaPlayer.setVolume(100,100);
        mediaPlayer.setLooping(false);
        //record
        recorder = new MediaRecorder(ClapDetectionService.this);
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile("/data/data/" + getPackageName() + "/music.3gp");
        //Timer
        handler = new Handler(Looper.getMainLooper());
        periodicUpdate = new Runnable() {
            @Override
            public void run() {
                finishAmplitude = recorder.getMaxAmplitude();
                if (finishAmplitude >= amplitudeThreshold) {
                    clapDetectedNumber++;
                }
                Log.d("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "run: " + clapDetectedNumber);
                if(timeClapEntity.isDoubleClap(clapDetectedNumber)) {
                    showToast();
                    clapDetectedNumber = 0;
                    handleClapDetected();
                }
                handler.postDelayed(this, delayMillis);
            }
        };
        timeClapEntity = new TimeClapEntity();
        registerNotificationChanel();
    }

    private void registerNotificationChanel() {
        // Đăng kí kênh thông báo
        builder = new NotificationCompat.Builder(this,NOTIFICATION_CHANEL_ID )
                .setContentTitle("Foreground Service")
                .setContentText("Service is running in the background")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Khởi tạo kênh thông báo (chỉ cần thực hiện một lần)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANEL_ID, "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(getApplicationContext(), "start service!", Toast.LENGTH_SHORT).show();

        try {
            recorder.prepare();
            recorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        handler.post(periodicUpdate);
        clapDetect();

        startForeground(NOTIFICATION_ID, builder.build());
        return  START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(getApplicationContext(), "stop service!", Toast.LENGTH_SHORT).show();
        mediaPlayer.stop();
        mediaPlayer.release();
        recorder.stop();
        recorder.release();
        handler.removeCallbacks(periodicUpdate);
    }

    private void handleClapDetected() {
        vibrate();
        ring();
    }


    private void showToast() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "schedule with " + clapDetectedNumber, Toast.LENGTH_LONG).show();
            }
        });
    }



    private void vibrate () {

        final int DELAY = 0, VIBRATE = 1000, SLEEP = 1000, START = -1;
        long[] vibratePattern = {DELAY, VIBRATE, SLEEP};
        //Rung
        VibratorManager vibratorManager = (VibratorManager) this.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
        Vibrator vibrator = vibratorManager.getDefaultVibrator();
        // this is the only type of the vibration which requires system version Oreo (API 26)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(vibratePattern, START));
        } else {
            // backward compatibility for Android API < 26
            // noinspection deprecation
            vibrator.vibrate(vibratePattern, START);
        }
    }

        private void ring() {
        mediaPlayer.start();
    }

    private void clapDetect() {
        // Tạo AudioDispatcher
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,512);
        double threshold = 12;
        double sensitivity = 50;
        PercussionOnsetDetector mPercussionDetector = new PercussionOnsetDetector(22050, 1024,
                new OnsetHandler() {
                    @Override
                    public void handleOnset(double time, double salience) {
                        Log.d("DDDDDDDDDDDDDDD", "clap detected!");
                    }
                }, sensitivity, threshold);
        dispatcher.addAudioProcessor(mPercussionDetector);
        new Thread(dispatcher,"Audio Dispatcher").start();
    }

}
