package com.duong_21011224.btl;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditProfileActivity extends AppCompatActivity {
    EditText etOldPassword, etNewUsername, etNewPassword;
    Button btnSave;
    ImageView btnBack;
    private final String UPDATE_URL = "http://172.20.10.8/BTL_DHT11/update_profile.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        etOldPassword = findViewById(R.id.etOldPassword);
        etNewUsername = findViewById(R.id.etNewUsername);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnBack=findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);
        btnBack.setOnClickListener(v->{
            Intent intent= new Intent(EditProfileActivity.this,SettingsActivity.class);
            startActivity(intent);
        });
        btnSave.setOnClickListener(v -> updateProfile());
    }
    private void updateProfile() {
        String oldPassword = etOldPassword.getText().toString();
        String newUsername = etNewUsername.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString();

        if (oldPassword.isEmpty() || newUsername.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String currentUsername = prefs.getString("logged_in_user", null);

        if (currentUsername == null) {
            Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("current_username", currentUsername);
            json.put("old_password", oldPassword);
            json.put("new_username", newUsername);
            json.put("new_password", newPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(UPDATE_URL).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(EditProfileActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String res = response.body().string();
                try {
                    JSONObject obj = new JSONObject(res);
                    String status = obj.getString("status");
                    String message = obj.getString("message");

                    runOnUiThread(() -> {
                        Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                        if (status.equals("success")) {
                            // Cập nhật SharedPreferences
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("logged_in_user", newUsername);
                            editor.apply();
                            finish(); // Quay lại màn hình trước
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}