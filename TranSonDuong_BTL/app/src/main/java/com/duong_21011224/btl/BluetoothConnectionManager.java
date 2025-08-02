package com.duong_21011224.btl;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;

public class BluetoothConnectionManager {
    private static BluetoothSocket bluetoothSocket;

    public static void setSocket(BluetoothSocket socket) {
        bluetoothSocket = socket;
    }

    public static BluetoothSocket getSocket() {
        return bluetoothSocket;
    }

    public static boolean isConnected() {
        return bluetoothSocket != null && bluetoothSocket.isConnected();
    }

    public static void closeSocket() {
        try {
            if (bluetoothSocket != null) bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        bluetoothSocket = null;
    }
}
