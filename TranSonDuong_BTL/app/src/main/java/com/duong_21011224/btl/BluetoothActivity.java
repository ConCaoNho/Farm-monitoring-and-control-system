package com.duong_21011224.btl;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice bluetoothDevice;

    private Switch switchBluetooth;
    ListView listPairedDevices, listAvailableDevices;
    ImageView btnBack;
    TextView tvTempValue, tvHumidValue;
    private final int REQUEST_ENABLE_BT = 1;
    private final int REQUEST_PERMISSIONS = 100;
    private static final int REQUEST_DISABLE_BT = 101;
    private ArrayAdapter<String> pairedAdapter, availableAdapter;
    private final ArrayList<String> pairedDevicesList = new ArrayList<>();
    private final ArrayList<String> availableDevicesList = new ArrayList<>();
    private final ArrayList<BluetoothDevice> availableDevices = new ArrayList<>();
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && device.getName() != null) {
                    String info = device.getName() + "\n" + device.getAddress();
                    if (!availableDevicesList.contains(info)) {
                        availableDevicesList.add(info);
                        availableDevices.add(device);
                        availableAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        switchBluetooth = findViewById(R.id.switchBluetooth);
        listPairedDevices = findViewById(R.id.listPairedDevices);
        listAvailableDevices = findViewById(R.id.listAvailableDevices);
        btnBack = findViewById(R.id.btnBack);
        tvTempValue= findViewById(R.id.tvTempValue);
        tvHumidValue=findViewById(R.id.tvHumidValue);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Thiết bị không hỗ trợ Bluetooth", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // List Adapter
        pairedAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pairedDevicesList);
        availableAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, availableDevicesList);
        listPairedDevices.setAdapter(pairedAdapter);
        listAvailableDevices.setAdapter(availableAdapter);

        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Đăng ký Receiver
        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        // Switch Bluetooth
        switchBluetooth.setChecked(bluetoothAdapter.isEnabled());
        switchBluetooth.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                    startActivityForResult(intent, REQUEST_DISABLE_BT);
                    Toast.makeText(this, "Vui lòng tắt Bluetooth thủ công, sau đó quay lại", Toast.LENGTH_LONG).show();
                } else {
                    bluetoothAdapter.disable();
                    disableBluetooth();
                }
            }
        });



        // Click item trong danh sách thiết bị khả dụng
        listAvailableDevices.setOnItemLongClickListener((parent, view, position, id) -> {
            BluetoothDevice device= availableDevices.get(position);

            new AlertDialog.Builder(BluetoothActivity.this)
                    .setTitle("Kết nối thiết bị")
                    .setMessage("Bạn có muốn kết nối với thiết bị không")
                    .setPositiveButton("Kết nối", (dialog,which) ->{
                        connectToDevice(device);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            return true;
        });
        listPairedDevices.setOnItemLongClickListener((parent, view, position, id) ->{
            @SuppressLint("MissingPermission")
            Set<BluetoothDevice> pairedSet = bluetoothAdapter.getBondedDevices();
            BluetoothDevice[] pairedArray = pairedSet.toArray(new BluetoothDevice[0]);

            if (position >= pairedArray.length) return true;

            BluetoothDevice device= pairedArray[position];

            new AlertDialog.Builder(BluetoothActivity.this)
                    .setTitle("Hủy ghép nối")
                    .setMessage("Bạn có muốn hủy kết nối với thiết bị" + device.getName()+"?")
                    .setPositiveButton("Đồng ý",(dialog,which)->{
                        unpairDevice(device);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            return true;
        });
        checkPermissions();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(bondStateReceiver, filter);

    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ArrayList<String> permissions = new ArrayList<>();
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.BLUETOOTH_SCAN);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

            if (!permissions.isEmpty()) {
                ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), REQUEST_PERMISSIONS);
            } else {
                loadDevices();
            }
        } else {
            loadDevices();
        }
    }

    @SuppressLint("MissingPermission")
    private void enableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
        } else {
            loadDevices();
        }
    }

    @SuppressLint("MissingPermission")
    private void disableBluetooth() {
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
        }
        pairedDevicesList.clear();
        availableDevicesList.clear();
        pairedAdapter.notifyDataSetChanged();
        availableAdapter.notifyDataSetChanged();
        disconnectFromDevice();
    }

    @SuppressLint("MissingPermission")
    private void loadDevices() {
        getPairedDevices();
        discoverDevices();
    }

    @SuppressLint("MissingPermission")
    private void getPairedDevices() {
        pairedDevicesList.clear();
        Set<BluetoothDevice> paired = bluetoothAdapter.getBondedDevices();
        if (paired != null && !paired.isEmpty()) {
            for (BluetoothDevice device : paired) {
                pairedDevicesList.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            pairedDevicesList.add("Không có thiết bị đã ghép nối");
        }
        pairedAdapter.notifyDataSetChanged();
    }

    @SuppressLint("MissingPermission")
    private void discoverDevices() {
        availableDevicesList.clear();
        availableDevices.clear();
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
    }

    @SuppressLint("MissingPermission")
    private void connectToDevice(BluetoothDevice device) {
        new Thread(() -> {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                bluetoothAdapter.cancelDiscovery();
                bluetoothSocket.connect();
                // 👉 Lưu vào manager
                BluetoothConnectionManager.setSocket(bluetoothSocket);
                startListeningForData();
                runOnUiThread(() -> Toast.makeText(this, "Đã kết nối với " + device.getName(), Toast.LENGTH_SHORT).show());

            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Kết nối thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
        }).start();
    }


    private void disconnectFromDevice() {
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
                bluetoothSocket = null;
                Toast.makeText(this, "Đã ngắt kết nối", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                loadDevices();
            } else {
                Toast.makeText(this, "Yêu cầu cấp quyền Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            loadDevices();
        }

        if (requestCode == REQUEST_DISABLE_BT) {
            // Khi quay về từ Bluetooth Settings
            if (!bluetoothAdapter.isEnabled()) {
                disableBluetooth(); // Dọn dẹp và cập nhật
            }
        }
    }
    @SuppressLint("MissingPermission")
    private void unpairDevice(BluetoothDevice device) {
        try {
            java.lang.reflect.Method method = device.getClass().getMethod("removeBond");
            method.invoke(device);
            Toast.makeText(this, "Đã hủy kết nối với " + device.getName(), Toast.LENGTH_SHORT).show();
            getPairedDevices(); // Cập nhật lại danh sách
        } catch (Exception e) {
            Toast.makeText(this, "Không thể hủy kết nối", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    private final BroadcastReceiver bondStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                getPairedDevices(); // Cập nhật lại danh sách khi trạng thái thay đổi
            }
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        if (bluetoothAdapter != null) {
            // Cập nhật trạng thái của switch theo trạng thái thực tế của Bluetooth
            switchBluetooth.setChecked(bluetoothAdapter.isEnabled());

            // Nếu Bluetooth bị tắt, dọn dẹp danh sách thiết bị
            if (!bluetoothAdapter.isEnabled()) {
                pairedDevicesList.clear();
                availableDevicesList.clear();
                pairedAdapter.notifyDataSetChanged();
                availableAdapter.notifyDataSetChanged();
            }
        }
    }
    private void startListeningForData() {
        new Thread(() -> {
            try {
                if (bluetoothSocket == null) {
                    Log.e("Bluetooth", "Mất kết nối Bluetooth");
                    runOnUiThread(() -> {
                        Toast.makeText(this, "⚠️ Mất kết nối thiết bị Bluetooth", Toast.LENGTH_SHORT).show();
                        finish(); // Quay về màn hình trước đó
                    });
                    return;
                }

                InputStream inputStream = bluetoothSocket.getInputStream();

                byte[] buffer = new byte[1024];
                int bytes;

                StringBuilder dataBuilder = new StringBuilder();

                while (true) {
                    bytes = inputStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    dataBuilder.append(incomingMessage);

                    String fullData = dataBuilder.toString();

                    if (fullData.contains("Temp:") && fullData.contains("Humi:") && fullData.contains(",")) {
                        runOnUiThread(() -> {
                            try {
                                String[] parts = fullData.split(",");
                                String tempPart = parts[0].split(":")[1].replace("*C", "").trim();
                                String humidPart = parts[1].split(":")[1].replace("%", "").trim();

                                tvTempValue.setText(tempPart + " °C");
                                tvHumidValue.setText(humidPart + " %");

                                dataBuilder.setLength(0); // Xoá buffer sau khi đã xử lý
                            } catch (Exception e) {
                                Toast.makeText(this, "❗Lỗi xử lý dữ liệu: " + fullData, Toast.LENGTH_SHORT).show();
                                dataBuilder.setLength(0); // Dọn dẹp buffer lỗi
                            }
                        });
                    }
                }

            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    finish();
                });
            }
        }).start();
    }

}