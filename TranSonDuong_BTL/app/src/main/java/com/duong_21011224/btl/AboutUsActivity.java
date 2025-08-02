package com.duong_21011224.btl;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class AboutUsActivity extends AppCompatActivity {
    TextView tvPhone, tvEmail;
    ImageView btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        tvPhone = findViewById(R.id.tvPhone);
        tvEmail = findViewById(R.id.tvEmail);
        btnBack= findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v->{
            Intent intent= new Intent(AboutUsActivity.this,SettingsActivity.class);
            startActivity(intent);
        });

        // Click gọi điện
        tvPhone.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+84359036307"));
            startActivity(intent);
        });

        // Click gửi email
        tvEmail.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:21011224@st.phenikaa-uni.edu.vn"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Support request");
            startActivity(Intent.createChooser(emailIntent, "Send Email"));
        });
    }
}