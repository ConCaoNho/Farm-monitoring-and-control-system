<?php
header("Content-Type: application/json; charset=UTF-8");

// Kết nối CSDL
$servername = "localhost";
$username = "root";       // Tùy hệ thống, thường là 'root'
$password = "";           // Mặc định XAMPP/MAMP không có mật khẩu
$dbname = "mydata_dht11"; // Tên database của bạn

$conn = new mysqli($servername, $username, $password, $dbname);

// Kiểm tra kết nối
if ($conn->connect_error) {
    echo json_encode(["status" => "error", "message" => "Lỗi kết nối: " . $conn->connect_error]);
    exit();
}

// Lấy bản ghi mới nhất
$sql = "SELECT Temperature, Humidity, Datatime FROM dht11 ORDER BY ID DESC LIMIT 1";
$result = $conn->query($sql);

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    echo json_encode([
        "status" => "success",
        "temperature" => $row["Temperature"],
        "humidity" => $row["Humidity"],
        "datatime" => $row["Datatime"]
    ]);
} else {
    echo json_encode(["status" => "error", "message" => "Không có dữ liệu"]);
}

$conn->close();
?>

