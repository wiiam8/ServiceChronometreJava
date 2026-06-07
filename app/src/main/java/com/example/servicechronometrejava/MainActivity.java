package com.example.servicechronometrejava;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private TextView tvTemps;
    private Button btnStart;
    private Button btnStop;

    private ChronometreService chronometreService;
    private boolean isBound = false;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            startChronometreService();
                        } else {
                            Toast.makeText(
                                    MainActivity.this,
                                    "Permission notification refusée",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ChronometreService.LocalBinder binder = (ChronometreService.LocalBinder) service;
            chronometreService = binder.getService();
            isBound = true;

            tvTemps.setText(chronometreService.getFormattedTime());

            chronometreService.setOnTimeChangedListener(new ChronometreService.OnTimeChangedListener() {
                @Override
                public void onTimeChanged(String time) {
                    tvTemps.setText(time);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            chronometreService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        tvTemps = findViewById(R.id.tvTemps);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkNotificationPermissionAndStart();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopChronometreService();
            }
        });
    }

    private void checkNotificationPermissionAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED) {
                startChronometreService();
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            startChronometreService();
        }
    }

    private void startChronometreService() {
        Intent intent = new Intent(this, ChronometreService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        Toast.makeText(this, "Service démarré", Toast.LENGTH_SHORT).show();
    }

    private void stopChronometreService() {
        if (isBound && chronometreService != null) {
            chronometreService.setOnTimeChangedListener(null);
            unbindService(connection);
            isBound = false;
        }

        Intent intent = new Intent(this, ChronometreService.class);
        intent.setAction(ChronometreService.ACTION_STOP);
        startService(intent);

        tvTemps.setText("00:00");

        Toast.makeText(this, "Service arrêté", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        if (isBound && chronometreService != null) {
            chronometreService.setOnTimeChangedListener(null);
            unbindService(connection);
            isBound = false;
        }

        super.onDestroy();
    }
}