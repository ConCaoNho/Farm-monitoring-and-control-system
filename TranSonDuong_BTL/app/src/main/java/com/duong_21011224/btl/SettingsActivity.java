package com.duong_21011224.btl;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class SettingsActivity extends AppCompatActivity {
    LinearLayout btnEditProflie, btnNotfications, btnAboutUs, btnLogout;
    ImageView btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        btnEditProflie=findViewById(R.id.btnEditProfile);
        btnNotfications=findViewById(R.id.btnNotifications);
        btnAboutUs=findViewById(R.id.btnAboutUs);
        btnLogout=findViewById(R.id.btnLogout);
        btnBack=findViewById(R.id.btnBack);
        // Quay lại màn hình trước
        btnBack.setOnClickListener( v->{
            Intent intent= new Intent(SettingsActivity.this,MainActivity.class);
            startActivity(intent);
        });
        // Chuyen sang cac Activity khac
        btnEditProflie.setOnClickListener( v->{
            Intent intent= new Intent(SettingsActivity.this,EditProfileActivity.class);
            startActivity(intent);
        });
        btnNotfications.setOnClickListener(v->{
            Intent intent= new Intent(SettingsActivity.this,NotificationsActivity.class);
            startActivity(intent);
        });
        btnAboutUs.setOnClickListener(v->{
            Intent intent= new Intent(SettingsActivity.this,AboutUsActivity.class);
            startActivity(intent);
        });
        btnLogout.setOnClickListener(v->{
            Intent intent = new Intent(SettingsActivity.this, LogoutActivity.class);
            startActivity(intent);
        });
    }
}