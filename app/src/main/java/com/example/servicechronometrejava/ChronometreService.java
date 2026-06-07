package com.example.servicechronometrejava;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChronometreService extends Service {

    public static final String ACTION_STOP = "STOP";
    public static final String CHANNEL_ID = "chrono_channel";
    public static final int NOTIFICATION_ID = 1001;

    private final IBinder binder = new LocalBinder();

    private int secondes = 0;
    private boolean isRunning = false;

    private ScheduledExecutorService executor;
    private NotificationManager notificationManager;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private OnTimeChangedListener listener;

    public interface OnTimeChangedListener {
        void onTimeChanged(String time);
    }

    public class LocalBinder extends Binder {
        public ChronometreService getService() {
            return ChronometreService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;

        if (ACTION_STOP.equals(action)) {
            stopSelf();
            return START_NOT_STICKY;
        }

        if (!isRunning) {
            isRunning = true;
            startForeground(NOTIFICATION_ID, createNotification());
            startChronometer();
        }

        return START_STICKY;
    }

    private void startChronometer() {
        executor = Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                secondes++;
                updateNotification();
                notifyActivity();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void notifyActivity() {
        if (listener != null) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onTimeChanged(getFormattedTime());
                }
            });
        }
    }

    private void updateNotification() {
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, createNotification());
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Chronomètre en cours")
                .setContentText("Temps : " + getFormattedTime())
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager != null) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Chronomètre Service",
                    NotificationManager.IMPORTANCE_LOW
            );

            channel.setDescription("Notification du service chronomètre");
            notificationManager.createNotificationChannel(channel);
        }
    }

    public String getFormattedTime() {
        int minutes = secondes / 60;
        int secondsRest = secondes % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, secondsRest);
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setOnTimeChangedListener(OnTimeChangedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        listener = null;

        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            executor = null;
        }

        stopForeground(true);
        super.onDestroy();
    }
}