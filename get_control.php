<?php
header("Content-Type: application/json; charset=UTF-8");

// Kết nối CSDL
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "mydata_dht11";

$conn = new mysqli($servername, $username, $password, $dbname);

// Kiểm tra kết nối
if ($conn->connect_error) {
    echo json_encode(["status" => "error", "message" => "Lỗi kết nối"]);
    exit();
}

// Lấy trạng thái hiện tại của led và fan từ bảng device_control
$sql = "SELECT device, state FROM device_control";
$result = $conn->query($sql);

$led = 0;
$fan = 0;

if ($result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        if ($row['device'] === 'led') {
            $led = (int)$row['state'];
        } else if ($row['device'] === 'fan') {
            $fan = (int)$row['state'];
        }
    }

    echo json_encode([
        "status" => "success",
        "led" => $led,
        "fan" => $fan
    ]);
} else {
    echo json_encode(["status" => "error", "message" => "Không có dữ liệu thiết bị"]);
}

$conn->close();
?>
