package com.example.clapdetection.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.filters.BandPass;
import be.tarsos.dsp.filters.HighPass;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.onsets.OnsetHandler;
import be.tarsos.dsp.onsets.PercussionOnsetDetector;

import com.example.clapdetection.R;

import java.util.Vector;

public class ClapDetectionService extends Service {
    MediaPlayer mediaPlayer;
    Handler handler;
    private static final String NOTIFICATION_CHANEL_ID = "ClapDetectionChanel";
    private static final  int NOTIFICATION_ID = 1;
    NotificationCompat.Builder builder;
    private static final int SAMPLE_RATE = 44100;
    private static final int BUFFER_SIZE = 2048;
    private static final int OVERLAP = BUFFER_SIZE / 2;
    private AudioDispatcher dispatcher;
    int minDistance = 200;
    int maxDistance = 1000;
    private int requiredClapCount = 3;
    private static ClapDetectionService instance = null;
    double threshold = 8;
    private  static double sensitivity = 50;
    Vector<Long> claps = new Vector<>();



    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // ring
        mediaPlayer = MediaPlayer.create(this, R.raw.ring2 );
        mediaPlayer.setVolume(100,100);
        mediaPlayer.setLooping(false);
        //use mic
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(SAMPLE_RATE,BUFFER_SIZE, OVERLAP);
        registerNotificationChanel();
    }



    public static boolean isInstanceCreated() {
        return instance != null;
    }//met

    public static void setSensitivity(double var1) {
        sensitivity = var1;
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
        instance = null;
        Toast.makeText(getApplicationContext(), "stop service!", Toast.LENGTH_SHORT).show();
        mediaPlayer.stop();
        mediaPlayer.release();
        dispatcher.stop();
    }

    private void onClapDetected() {
        vibrate();
        ring();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void showToast(String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
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
            return;
        }
        // noinspection deprecation
        vibrator.vibrate(vibratePattern, START);

    }

    private void ring() {
    mediaPlayer.start();
}

    private void clapDetect() {
        dispatcher.addAudioProcessor(new BandPass(3000,SAMPLE_RATE / 2, SAMPLE_RATE));
        dispatcher.addAudioProcessor(createPercussionOnsetDetector());
        Thread thread = new Thread(dispatcher, "Audio Dispatcher");
        thread.start();
    }


    private PercussionOnsetDetector createPercussionOnsetDetector() {
        PercussionOnsetDetector mPercussionDetector = new PercussionOnsetDetector(SAMPLE_RATE, BUFFER_SIZE,
            new OnsetHandler() {
                @Override
                public void handleOnset(double time, double salience) {
                    handleSound();
//                    showToast(mé);
                    Log.d("sound detected", "detected!");
                }
            }, sensitivity, threshold);
        return mPercussionDetector;
    }


    private void handleSound() {
        long currentTime = System.currentTimeMillis();
        if(claps.size() > 0 && currentTime - claps.get(claps.size() - 1) > maxDistance) {
            claps.clear();
        }
        claps.add(currentTime);
        if(claps.size() > 1 && claps.get(claps.size() - 1) - claps.get(claps.size() -2) < minDistance) {
            claps.remove(claps.size() - 1);
        }
        Log.i("Mitch","claps = " + claps.size());
        if(claps.size() >= requiredClapCount){
            onClapDetected();
        }

    }
}
