package com.duong_21011224.btl;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    TextView tvTemp,tvHumid,tvUser;
    Switch switchLed, switchFan, switchAuto;
    ImageView ivBulb, ivFan;
    String SERVER_URL="http://172.20.10.8/BTL_DHT11/get_data.php";
    String CONTROL_URL ="http://172.20.10.8/BTL_DHT11/control.php";
    Handler handler= new Handler(Looper.getMainLooper());
    Runnable dataUpdater;
    private static final int TEMP_HIGH_THRESHOLD = 35;
    private static final int TEMP_LOW_THRESHOLD = 10;
    private static final int HUMID_HIGH_THRESHOLD = 70;
    private static final int HUMID_LOW_THRESHOLD = 20;
    private boolean isAutoMode = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvTemp=findViewById(R.id.tvTempValue);
        tvHumid=findViewById(R.id.tvHumidValue);
        switchLed=findViewById(R.id.switchLed);
        switchFan=findViewById(R.id.switchFan);
        ivBulb= findViewById(R.id.ivBulb);
        ivFan= findViewById(R.id.ivFan);
        tvUser=findViewById(R.id.tvUser);
        switchAuto= findViewById(R.id.auto);
        switchAuto.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isAutoMode = isChecked;
            Toast.makeText(this, isChecked ? "Chế độ TỰ ĐỘNG bật" : "Chế độ TỰ ĐỘNG tắt", Toast.LENGTH_SHORT).show();
        });
        //String role = getIntent().getStringExtra("role");
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String role = prefs.getString("user_role", "guest");// mặc định là guest nếu không tìm thấy
        Log.d("MAIN_ROLE", "Loaded role: " + role);

        String username = prefs.getString("logged_in_user", role);
        tvUser.setText("Xin chào " + username);
        LinearLayout navManageUsers = findViewById(R.id.nav_manage_users);

        if ("admin".equals(role)) {
            navManageUsers.setVisibility(View.VISIBLE);
        }

        //Switch Led
        switchLed.setOnCheckedChangeListener((buttonView, isChecked) ->{
            ivBulb.setImageResource(isChecked ? R.drawable.ic_bulb_on :R.drawable.ic_bulb_off);
            controlDevice("led",isChecked ?1:0);
        });
        switchFan.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ivFan.setImageResource(isChecked ? R.drawable.fan_on : R.drawable.fan_off);
            controlDevice("fan", isChecked ? 1 : 0);
        });
        // Tự động cập nhật dữ liệu mỗi 3 giây
        dataUpdater = new Runnable() {
            @Override
            public void run() {
                fetchSensorData();
                handler.postDelayed(this, 3000);
            }
        };
        handler.post(dataUpdater);
        // Điều hướng giữa các mục (Trang chủ - Cài đặt - Dữ liệu)
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navSetting = findViewById(R.id.nav_setting);
        LinearLayout navData = findViewById(R.id.nav_data);
        navHome.setOnClickListener(v->{
            //MainActivity
        });
        navSetting.setOnClickListener(v ->{
            Intent intent= new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(intent);
        });
        navData.setOnClickListener(v ->{
            Intent intent = new Intent(MainActivity.this, DataActivity.class);
            startActivity(intent);
        });
        navManageUsers.setOnClickListener(v -> {
            // mở màn hình quản lý tài khoản hoặc fragment tương ứng
            Intent intent = new Intent(MainActivity.this, AdminActivity.class);
            startActivity(intent);
        });
        // khoi dong service khi app chay
        Intent serviceIntent = new Intent(this, SensorMonitorService.class);
        startService(serviceIntent);

        //Goi ham lay du lieu
        fetchSensorData();
    }
    //Gui lenh bat tat led hoac fan len server
    private void controlDevice(String device, int status){
        OkHttpClient client = new OkHttpClient();
        String url = CONTROL_URL + "?device=" + device + "&state=" + status;
        Request request= new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(()-> Toast.makeText(MainActivity.this,"Loi ket noi dieu khien",Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(()->Log.d("Control",device+" set to" + status));
            }
        });
    }
    // Lấy dữ liệu nhiệt độ và độ ẩm mới nhất từ server
    private void fetchSensorData() {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(SERVER_URL)
                .build();

        // Gửi request bất đồng bộ
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();

                    try {
                        JSONObject json = new JSONObject(responseData);
                        if (json.getString("status").equals("success")) {
                            final String temperature = json.getString("temperature");
                            final String humidity = json.getString("humidity");

                            runOnUiThread(() -> {
                                tvTemp.setText(temperature + " °C");
                                tvHumid.setText(humidity + " %");
                                try {
                                    float temp = Float.parseFloat(temperature);
                                    float humid = Float.parseFloat(humidity);
                                    String message = null;

                                    if (temp > TEMP_HIGH_THRESHOLD) {
                                        message = "Nhiệt độ quá cao: " + temp + "°C";
                                    } else if (temp < TEMP_LOW_THRESHOLD) {
                                        message = "Nhiệt độ quá thấp: " + temp + "°C";
                                    } else if (humid > HUMID_HIGH_THRESHOLD) {
                                        message = "Độ ẩm quá cao: " + humid + "%";
                                    } else if (humid < HUMID_LOW_THRESHOLD) {
                                        message = "Độ ẩm quá thấp: " + humid + "%";
                                    }

                                    if (message != null) {
                                        sendNotification(message);
                                    }
                                    // === Logic điều khiển tự động ===

                                    boolean shouldTurnOnFan = temp > TEMP_HIGH_THRESHOLD || humid > HUMID_HIGH_THRESHOLD;
                                    boolean shouldTurnOnLed = temp > TEMP_HIGH_THRESHOLD || temp < TEMP_LOW_THRESHOLD;
                                    if (isAutoMode){
                                    // Bật/tắt quạt
                                    if (shouldTurnOnFan) {
                                        if (!switchFan.isChecked()) {
                                            switchFan.setChecked(true); // sẽ kích hoạt listener để gửi lệnh
                                        }
                                    } else {
                                        if (switchFan.isChecked()) {
                                            switchFan.setChecked(false);
                                        }
                                    }

                                    // Bật/tắt đèn
                                    if (shouldTurnOnLed) {
                                        if (!switchLed.isChecked()) {
                                            switchLed.setChecked(true);
                                        }
                                    } else {
                                        if (switchLed.isChecked()) {
                                            switchLed.setChecked(false);
                                        }
                                    }}
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                            });
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "Không có dữ liệu", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(dataUpdater); // Ngừng cập nhật khi thoát Activity
    }
    private void sendNotification(String message) {
        saveNotificationLog(message);

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String username = prefs.getString("logged_in_user", null);

        Log.d("MainActivity", "Gửi thông báo cho user: " + username);

        Intent intent = new Intent(this, NotificationService.class);
        intent.putExtra("message", message);
        intent.putExtra("username", username);  // Gửi username đi
        startService(intent);
    }

    private void saveNotificationLog(String message) {
        SharedPreferences prefs = getSharedPreferences("notifications", MODE_PRIVATE);
        String existingLogs = prefs.getString("logs", "");
        String newLog = System.currentTimeMillis() + ": " + message;

        existingLogs = existingLogs + newLog + "\n";

        prefs.edit().putString("logs", existingLogs).apply();
    }


}