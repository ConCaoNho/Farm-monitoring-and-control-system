<?php

if (!isset($_GET['device']) || !isset($_GET['state'])) {
    echo "Thiếu tham số!";
    exit();
}

$device = $_GET['device']; // "led" hoặc "fan"
$state = intval($_GET['state']); // 1 = BẬT, 0 = TẮT

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "mydata_dht11";

$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    echo "Lỗi kết nối: " . $conn->connect_error;
    exit();
}

// Nếu đã tồn tại thiết bị, cập nhật. Nếu chưa, thêm mới
$sql = "INSERT INTO device_control (device, state) VALUES ('$device', $state)
        ON DUPLICATE KEY UPDATE state = $state";

if ($conn->query($sql) === TRUE) {
    echo "OK";
} else {
    echo "Lỗi: " . $conn->error;
}

$conn->close();
?>
