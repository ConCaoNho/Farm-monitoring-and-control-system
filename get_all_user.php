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
    echo json_encode([
        "status" => "error",
        "message" => "Lỗi kết nối: " . $conn->connect_error
    ]);
    exit();
}

// Truy vấn tất cả người dùng, ẩn mật khẩu
$sql = "SELECT id, username, role, email, created_by, created_at FROM users";
$result = $conn->query($sql);

$users = [];

if ($result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $users[] = [
            "id" => $row["id"],
            "username" => $row["username"],
            "role" => $row["role"],
            "email" => $row["email"],
            "created_by" => $row["created_by"],
            "created_at" => $row["created_at"]
        ];
    }
    echo json_encode([
        "status" => "success",
        "data" => $users
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Không có tài khoản nào."
    ]);
}

$conn->close();
?>
