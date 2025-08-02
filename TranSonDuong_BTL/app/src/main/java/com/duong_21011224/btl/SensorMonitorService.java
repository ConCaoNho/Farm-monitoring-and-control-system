package com.duong_21011224.btl;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SensorMonitorService extends Service {
    private Handler handler = new Handler();
    private Runnable runnable;
    private static final long INTERVAL = 3000; // 3 giây

    private String SERVER_URL = "http://172.20.10.8/BTL_DHT11/get_data.php";
    private static final int TEMP_HIGH = 35, TEMP_LOW = 10;
    private static final int HUMID_HIGH = 70, HUMID_LOW = 20;

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "SensorChannel", "Cảnh báo cảm biến", NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, "SensorChannel")
                    .setContentTitle("")
                    .setContentText("") // <-- để trống
                    .setSmallIcon(R.drawable.ic_notification)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setOngoing(true)
                    .build();

            startForeground(2, notification);
        }
        runnable = () -> {
            fetchData();
            handler.postDelayed(runnable, INTERVAL);
        };
        handler.post(runnable);

        return START_STICKY;
    }

    private void fetchData() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(SERVER_URL).build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { e.printStackTrace(); }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) return;
                String res = response.body().string();
                try {
                    JSONObject json = new JSONObject(res);
                    if (json.getString("status").equals("success")) {
                        float temp = Float.parseFloat(json.getString("temperature"));
                        float humid = Float.parseFloat(json.getString("humidity"));
                        String message = null;

                        if (temp > TEMP_HIGH) message = "Nhiệt độ cao: " + temp + "°C";
                        else if (temp < TEMP_LOW) message = "Nhiệt độ thấp: " + temp + "°C";
                        else if (humid > HUMID_HIGH) message = "Độ ẩm cao: " + humid + "%";
                        else if (humid < HUMID_LOW) message = "Độ ẩm thấp: " + humid + "%";

                        if (message != null) {
                            sendAlert(message);
                        }
                    }
                } catch (JSONException | NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void sendAlert(String message) {
        Intent intent = new Intent(this, NotificationService.class);
        intent.putExtra("message", message);

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String username = prefs.getString("logged_in_user", null);
        intent.putExtra("username", username);

        startService(intent);
    }


    @Override public void onDestroy() {
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}

