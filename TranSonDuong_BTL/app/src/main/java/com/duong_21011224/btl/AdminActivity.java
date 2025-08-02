package com.duong_21011224.btl;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.PixelCopy;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class AdminActivity extends AppCompatActivity {
    private RecyclerView recyclerAccounts;
    private Button btnAddUser;
    private AccountAdapter adapter;
    private String currentAdminUsername;
    private Context context;
    private List<Account> accountList = new ArrayList<>();
    private final String getAllUserUrl = "http://172.20.10.8/BTL_DHT11/get_all_user.php";
    private String currentUsername;
    ImageView btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        recyclerAccounts = findViewById(R.id.recyclerAccounts);
        btnAddUser = findViewById(R.id.btnAddUser);
        btnBack=findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v->{
            Intent intent = new Intent(AdminActivity.this,MainActivity.class);
            startActivity(intent);
        });

        currentUsername = getIntent().getStringExtra("username");

        recyclerAccounts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AccountAdapter(
                AdminActivity.this,        // context
                accountList,               // danh sách tài khoản
                currentUsername,           // username hiện tại (admin)
                new AccountAdapter.OnAccountActionListener() { // listener xử lý sự kiện
                    @Override
                    public void onToggleRole(Account account) {
                        String role_url=  "http://172.20.10.8/BTL_DHT11/update_user_role.php";
                        String newRole = account.getRole().equals("admin")?"guest":"admin";
                        JSONObject json = new JSONObject();
                        try {
                            json.put("id", account.getId());
                            json.put("role",newRole);

                        }catch (Exception e){
                            e.printStackTrace();
                            return;
                        }
                        RequestBody body= RequestBody.create(json.toString(), okhttp3.MediaType.parse("application/json"));
                        Request request = new Request.Builder().url(role_url).post(body).build();
                        OkHttpClient client= new OkHttpClient();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                runOnUiThread(()-> Toast.makeText(AdminActivity.this,"Loi ket noi server", Toast.LENGTH_SHORT).show());
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                runOnUiThread(()->{
                                    Toast.makeText(AdminActivity.this,"Cap nhat quyen thanh cong", Toast.LENGTH_SHORT).show();
                                    loadUsers();
                                });
                            }
                        });
                    }

                    @Override
                    public void onDelete(Account account) {
                        String delete_url = "http://172.20.10.8/BTL_DHT11/delete_user.php";
                        JSONObject json = new JSONObject();
                        try {
                            json.put("id", account.getId());
                            json.put("username", account.getUsername());
                            json.put("role", account.getRole());
                            json.put("requester", account.getUsername()); // thêm requester

                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }

                        RequestBody body = RequestBody.create(json.toString(), okhttp3.MediaType.parse("application/json"));
                        Request request = new Request.Builder().url(delete_url).post(body).build();
                        OkHttpClient client = new OkHttpClient();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                runOnUiThread(() -> Toast.makeText(AdminActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show());
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                String resStr = response.body().string();
                                runOnUiThread(() -> {
                                    Toast.makeText(AdminActivity.this, "Xoá tài khoản thành công", Toast.LENGTH_SHORT).show();
                                    loadUsers();
                                });
                            }
                        });
                    }

                }
        );
        recyclerAccounts.setAdapter(adapter);

        btnAddUser.setOnClickListener(v -> startActivity(new Intent(this, CreateUserActivity.class)));

        loadUsers();
    }
    private void loadUsers() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(getAllUserUrl).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(AdminActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resStr = response.body().string();
                try {
                    JSONObject resJson = new JSONObject(resStr);
                    if (resJson.getString("status").equals("success")) {
                        JSONArray users = resJson.getJSONArray("data");
                        accountList.clear();
                        for (int i = 0; i < users.length(); i++) {
                            JSONObject u = users.getJSONObject(i);
                            Account account = new Account(
                                    u.getInt("id"),
                                    u.getString("username"),
                                    "", // Không có password trả về từ JSON
                                    u.getString("role"),
                                    u.optString("email", "")
                            );
                            accountList.add(account);
                        }
                        runOnUiThread(() -> adapter.notifyDataSetChanged());
                    } else {
                        runOnUiThread(() -> Toast.makeText(AdminActivity.this, "Lỗi dữ liệu người dùng", Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(AdminActivity.this, "Lỗi xử lý phản hồi", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}