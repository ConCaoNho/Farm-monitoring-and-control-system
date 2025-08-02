package com.duong_21011224.btl;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.cert.CertPathBuilderSpi;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin, BLE;
    private final String loginUrl= "http://172.20.10.8/BTL_DHT11/login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        TextView tvRegister=findViewById(R.id.tvRegister);
        TextView tvForgotPassword= findViewById(R.id.tvForgotPassword);
        etUsername = findViewById(R.id.etUsername);
        etPassword= findViewById(R.id.etPassword);
        btnLogin= findViewById(R.id.btnLogin);
        BLE= findViewById(R.id.BLE);
        btnLogin.setOnClickListener(v -> loginUser());
        tvRegister.setOnClickListener(v->{
            Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
            startActivity(intent);
        });
        tvForgotPassword.setOnClickListener(v->{
            Intent intent= new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
        BLE.setOnClickListener(v->{
            Intent intent= new Intent(LoginActivity.this,BluetoothActivity.class);
            startActivity(intent);
        });

    }
    private void loginUser(){
        String username= etUsername.getText().toString().trim();
        String password= etPassword.getText().toString().trim();
        if (username.isEmpty() || password.isEmpty()){
            Toast.makeText(this,"Vui lòng nhập đầy đủ thông tin",Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject json= new JSONObject();
        try {
            json.put("username",username);
            json.put("password",password);
        } catch (JSONException e){
            e.printStackTrace();
        }
        RequestBody body= RequestBody.create(json.toString(), MediaType.parse("application/json"));
        Request request= new Request.Builder()
                .url(loginUrl)
                .post(body)
                .build();
        OkHttpClient client= new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(()-> Toast.makeText(LoginActivity.this,"Lỗi kết nối server",Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String resStr= response.body().string();
                runOnUiThread(()->{
                    try {
                        JSONObject resJson= new JSONObject(resStr);
                        if (resJson.getString("status").equals("success")){
                            String role=resJson.getString("role");
                            Toast.makeText(LoginActivity.this,"Đăng nhập thành công",Toast.LENGTH_SHORT).show();

                            // ✅ LƯU USERNAME VÀO SharedPreferences
                            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("logged_in_user", username);  // <- lưu username
                            editor.putString("user_role",role);  //giu dang nhap sau khi dong app
                            editor.putBoolean("isLoggedIn", true);
                            editor.apply();
                            Log.d("LOGIN_ROLE", "Saved role: " + role);
                            Log.d("LoginDebug", "Đã lưu username: " + username);
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("username", username);
                            intent.putExtra("role", role); // truyền role (admin hoặc guest)
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this,resJson.getString("message"),Toast.LENGTH_SHORT).show();

                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this,"Lỗi xử lý phản hồi",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}