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
    echo json_encode(["status" => "error", "message" => "Lỗi kết nối CSDL"]);
    exit();
}

// Nhận dữ liệu JSON từ client
$data = json_decode(file_get_contents("php://input"), true);

$username = trim($data["username"]);
$password = $data["password"];
$role = $data["role"];
$email = trim($data["email"]);
$created_by = trim($data["created_by"]);

// Kiểm tra đầu vào
if (empty($username) || empty($password) || empty($role) || empty($email)) {
    echo json_encode(["status" => "error", "message" => "Thiếu thông tin"]);
    exit();
}

// Không cho tạo trùng username
$check = $conn->prepare("SELECT id FROM users WHERE username = ?");
$check->bind_param("s", $username);
$check->execute();
$check->store_result();

if ($check->num_rows > 0) {
    echo json_encode(["status" => "error", "message" => "Tên người dùng đã tồn tại"]);
    exit();
}

// Mã hóa mật khẩu
$hashedPassword = password_hash($password, PASSWORD_BCRYPT);

// Tạo user mới
$stmt = $conn->prepare("INSERT INTO users (username, password, role, email, created_by, created_at) VALUES (?, ?, ?, ?, ?, NOW())");
$stmt->bind_param("sssss", $username, $hashedPassword, $role, $email, $created_by);

if ($stmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Tạo tài khoản thành công"]);
} else {
    echo json_encode(["status" => "error", "message" => "Lỗi khi tạo tài khoản"]);
}

$conn->close();
?>
