package com.duong_21011224.btl;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

public class LogoutActivity extends AppCompatActivity {
    Button btnYes, btnNo;
    ImageView btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);
        btnYes=findViewById(R.id.btnYes);
        btnNo=findViewById(R.id.btnNo);
        btnBack=findViewById(R.id.btnBack);
        btnYes.setOnClickListener(v->{
            // 1. Dừng foreground service
            Intent stopServiceIntent = new Intent(LogoutActivity.this, NotificationService.class);
            stopService(stopServiceIntent);
            // 2. Dừng SensorMonitorService (foreground service có hiển thị notification)
            Intent stopSensorService = new Intent(LogoutActivity.this, SensorMonitorService.class);
            stopService(stopSensorService);
            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            prefs.edit().clear().apply(); // Xóa toàn bộ thông tin đăng nhập

            // Đăng xuất: chuyển về màn hình đăng nhập
            Intent intent = new Intent(LogoutActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        btnNo.setOnClickListener(v->{
            Intent intent = new Intent(LogoutActivity.this,SettingsActivity.class);
            startActivity(intent);
        });
        btnBack.setOnClickListener(v->{
            Intent intent= new Intent(LogoutActivity.this,SettingsActivity.class);
            startActivity(intent);
        });
    }
}