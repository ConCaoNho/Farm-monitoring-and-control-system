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
    echo json_encode(["status" => "error", "message" => "Lỗi kết nối: " . $conn->connect_error]);
    exit();
}

// Lấy nhiều bản ghi, giới hạn 50 bản ghi gần nhất
$sql = "SELECT Temperature, Humidity, Datatime FROM dht11 ORDER BY ID DESC LIMIT 50";
$result = $conn->query($sql);

$data = [];

if ($result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $data[] = [
            "temperature" => floatval($row["Temperature"]),
            "humidity" => floatval($row["Humidity"]),
            "timestamp" => $row["Datatime"]
        ];
    }
    echo json_encode(["status" => "success", "data" => array_reverse($data)]); // Đảo ngược để thời gian tăng dần
} else {
    echo json_encode(["status" => "error", "message" => "Không có dữ liệu"]);
}

$conn->close();
?>
