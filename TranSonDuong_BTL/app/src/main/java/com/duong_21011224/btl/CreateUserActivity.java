package com.duong_21011224.btl;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Inet4Address;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateUserActivity extends AppCompatActivity {
    ImageView btnBack;
    EditText etUsername, etPassword, etEmail;
    RadioGroup roleGroup;
    Button btnCreateUser;
    private final String CREATE_URL = "http://172.20.10.8/BTL_DHT11/create_user.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);
        btnBack=findViewById(R.id.btnBack);
        etUsername=findViewById(R.id.etUsername);
        etPassword=findViewById(R.id.etPassword);
        etEmail=findViewById(R.id.etEmail);
        roleGroup= findViewById(R.id.roleGroup);
        btnCreateUser= findViewById(R.id.btnCreateUser);
        btnBack.setOnClickListener(v->{
            Intent intent=new Intent(CreateUserActivity.this, AdminActivity.class);
            startActivity(intent);
        });
        btnCreateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewUser();
            }
        });


    }
    private  void createNewUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();
        String email = etEmail.getText().toString().trim();
        String role = "";
        int selectedId = roleGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.radioAdmin) {
            role = "admin";
        } else if (selectedId == R.id.radioGuest) {
            role = "guest";
        }
        // Lay nguoi tao tu SharePreferences
        SharedPreferences pref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String creatBy = pref.getString("logged_in_user", "unknown");
        // Kiem tra dau vao
        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject json = new JSONObject();
        try {
            json.put("username", username);
            json.put("password", password);
            json.put("role", role);
            json.put("email", email);
            json.put("created_by", creatBy);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );
        Request request = new Request.Builder()
                .url(CREATE_URL)
                .post(body)
                .build();
        OkHttpClient client= new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->{
                    Toast.makeText(CreateUserActivity.this,"Loi ket noi server", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(CreateUserActivity.this, "Lỗi từ server", Toast.LENGTH_SHORT).show()
                    );
                    return;
            }
                String res= response.body().string();
                try {
                    JSONObject obj= new JSONObject(res);
                    String status= obj.getString("status");
                    String message= obj.getString("message");
                     runOnUiThread(()->{
                         Toast.makeText(CreateUserActivity.this,message,Toast.LENGTH_LONG).show();
                         if (status.equals("success")){
                             etUsername.setText("");
                             etPassword.setText("");
                             etEmail.setText("");
                             roleGroup.clearCheck();
                             // Quay về AdminActivity sau khi tạo xong
                             Intent intent = new Intent(CreateUserActivity.this, MainActivity.class);
                             intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // Xóa các activity trước đó và chỉ tạo 1 instance
                             intent.putExtra("refresh", true);  // Gửi cờ yêu cầu AdminActivity reload
                             startActivity(intent);
                             finish();  // Kết thúc CreateUserActivity
                         }
                     });
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }

        });
    }
}