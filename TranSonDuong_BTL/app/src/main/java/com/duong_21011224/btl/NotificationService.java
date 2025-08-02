package com.duong_21011224.btl;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class NotificationService extends Service {
    private static final String CHANNEL_ID = "AlertChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String message = intent.getStringExtra("message");
        String username = intent.getStringExtra("username");
        Log.d("NotificationDebug", "Username nhận được: " + username);
        // 🔸 Lưu thông báo vào SharedPreferences gắn theo username
        if (username != null && message != null) {
            SharedPreferences prefs = getSharedPreferences("MyNotifications", MODE_PRIVATE);
            String existing = prefs.getString(username, "");
            String updated = existing + "- " + message + "\n";
            prefs.edit().putString(username, updated).apply();
        }
        Intent notificationIntent = new Intent(this, NotificationsActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        /// ✅ Gửi đúng người trong PendingIntent
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String loggedInUser = prefs.getString("logged_in_user", null);
        notificationIntent.putExtra("username", loggedInUser);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Thông báo hệ thống")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_warning)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[]{0, 500, 1000})  // rung
                .build();

        // Rung thiết bị
        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (v != null && v.hasVibrator()) {
            v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
        }

        startForeground(1, notification);
        stopSelf(); // Dừng sau khi thông báo
        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Thông báo cảnh báo";
            String description = "Kênh dùng cho cảnh báo nhiệt độ/độ ẩm";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

