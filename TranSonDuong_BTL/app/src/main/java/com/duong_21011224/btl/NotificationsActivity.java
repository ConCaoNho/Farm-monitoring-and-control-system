package com.duong_21011224.btl;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    RecyclerView recyclerNotifications;
    NotificationAdapter adapter;
    List<String> notificationList;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        recyclerNotifications = findViewById(R.id.recyclerNotifications);
        btnBack = findViewById(R.id.btnBack);

        // Quay lại màn hình trước
        btnBack.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        if (username == null) {
            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            username = prefs.getString("logged_in_user", null);
        }
        Log.d("NotificationsDebug", "Username trong NotificationsActivity: " + username);
        SharedPreferences prefs = getSharedPreferences("MyNotifications", MODE_PRIVATE);
        String allNotifications = prefs.getString(username, "Chưa có thông báo");
        String logs = prefs.getString(username, "");
        Log.d("NotificationDebug", "Username nhận được: " + username);

        // Chuyển thành list
        if (!logs.isEmpty()) {
            String[] lines = logs.split("\n");
            notificationList = new ArrayList<>(Arrays.asList(lines));
            Collections.reverse(notificationList); // Hiển thị thông báo mới nhất trước
        } else {
            notificationList = new ArrayList<>();
            notificationList.add("Chưa có thông báo nào.");
        }

        // Set adapter cho RecyclerView
        adapter = new NotificationAdapter(notificationList);
        recyclerNotifications.setLayoutManager(new LinearLayoutManager(this));
        recyclerNotifications.setAdapter(adapter);
    }
}
