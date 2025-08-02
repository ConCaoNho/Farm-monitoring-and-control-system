package com.duong_21011224.btl;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DataActivity extends AppCompatActivity {

    LineChart lineChartTemp, lineChartHum;
    ImageView btnBack;
    String dataUrl="http://172.20.10.8/BTL_DHT11/get_all_data.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        lineChartTemp = findViewById(R.id.lineChartTemperature);
        lineChartHum=findViewById(R.id.lineChartHumidity);
        btnBack=findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v->{
            Intent intent = new Intent(DataActivity.this,MainActivity.class);
            startActivity(intent);
        });

        Handler handler = new Handler();
        Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                fetchChartData();
                handler.postDelayed(this, 5000); // cập nhật mỗi 5 giây
            }
        };
        handler.post(updateTask);

    }
    private void fetchChartData(){
        new Thread(()->{
            try {
                URL url= new URL(dataUrl);
                HttpURLConnection conn= (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader in= new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response= new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) response.append(line);
                in.close();

                JSONObject json= new JSONObject(response.toString());
                if (json.getString("status").equals("success")){
                    JSONArray dataArray = json.getJSONArray("data");

                    ArrayList<Entry> tempEntries = new ArrayList<>();
                    ArrayList<Entry> humEntries = new ArrayList<>();

                    int total = dataArray.length();
                    int startIndex = Math.max(0, total - 20);  // ✅ Chỉ lấy 20 điểm cuối
                    List<String> timestamps = new ArrayList<>();

                    for (int i = startIndex; i < total; i++) {
                        JSONObject obj = dataArray.getJSONObject(i);
                        float temp = (float) obj.getDouble("temperature");
                        float hum = (float) obj.getDouble("humidity");
                        String time = obj.getString("timestamp");
                        timestamps.add(time); // dùng cho marker

                        tempEntries.add(new Entry(i - startIndex, temp));  // index từ 0
                        humEntries.add(new Entry(i - startIndex, hum));
                    }

                    runOnUiThread(() -> {
                        showLineChart(lineChartTemp, tempEntries, "Nhiệt độ (°C)", "#FF5722", timestamps);
                        showLineChart(lineChartHum, humEntries, "Độ ẩm (%)", "#2196F3", timestamps);
                    });
                }else {
                    showToast("Không có dữ liệu!");
                }
            } catch (Exception e){
                e.printStackTrace();
                showToast("Lỗi lấy dữ liệu từ server");
            }
        }).start();
    }
    private void showLineChart(LineChart chart, ArrayList<Entry> entries, String label, String colorHex, List<String> timestamps) {
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(android.graphics.Color.parseColor(colorHex));
        dataSet.setCircleColor(android.graphics.Color.parseColor(colorHex));
        dataSet.setLineWidth(1.5f);
        dataSet.setCircleRadius(2f);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        CustomMarkerView markerView = new CustomMarkerView(this, R.layout.custom_marker_view, timestamps);
        markerView.setChartView(chart); // Quan trọng!
        chart.setMarker(markerView);

        chart.getDescription().setEnabled(false);
        chart.getLegend().setTextSize(12f);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setTextSize(12f);

        // ✅ Giới hạn trục Y từ 0 đến 100
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(100f);
        yAxis.setDrawGridLines(true);
        yAxis.setTextSize(12f);

        chart.getAxisRight().setEnabled(false); // Tắt trục Y phải
        chart.animateX(10);

        chart.invalidate();
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(DataActivity.this, message, Toast.LENGTH_SHORT).show());
    }

}