package com.duong_21011224.btl;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String username = prefs.getString("logged_in_user", null);
        String role = prefs.getString("user_role", null);  // Nếu có lưu role

        if (username != null && role != null) {
            // ✅ Đã đăng nhập → vào thẳng MainActivity
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("role", role);
            startActivity(intent);
            finish();
        } else {
            // ❌ Chưa đăng nhập → hỏi người dùng
            showStartDialog();
        }
    }
    private void showStartDialog(){
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận truy cập");
        builder.setMessage("Bạn có muốn mở ứng dụng không?");
        builder.setCancelable(false);
        builder.setPositiveButton("Cho phép", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent= new Intent(StartActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        AlertDialog dialog= builder.create();
        dialog.show();
    }
}