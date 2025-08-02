<?php
header('Content-Type: application/json');

// Kết nối MySQL
$conn = new mysqli("localhost", "root", "", "mydata_dht11");
if ($conn->connect_error) {
    echo json_encode(["status" => "error", "message" => "Lỗi kết nối DB"]);
    exit();
}

// Nhận dữ liệu JSON từ Android gửi lên
$data = json_decode(file_get_contents("php://input"), true);
$username = $data["username"];
$password = $data["password"];
$email = $data["email"];  // ✅ Lấy từ JSON, KHÔNG dùng $_POST
$role = $data["role"];

// Kiểm tra tài khoản đã tồn tại chưa
$stmt = $conn->prepare("SELECT id FROM users WHERE username = ?");
$stmt->bind_param("s", $username);
$stmt->execute();
$stmt->store_result();

if ($stmt->num_rows > 0) {
    echo json_encode(["status" => "error", "message" => "Tài khoản đã tồn tại"]);
    exit();
}

// Nếu chưa tồn tại, thực hiện INSERT
$hashedPassword = password_hash($password, PASSWORD_DEFAULT);
$created_at = date('Y-m-d H:i:s');

$stmt = $conn->prepare("INSERT INTO users (username, password, email, role, created_at) VALUES (?, ?, ?, ?, ?)");
$stmt->bind_param("sssss", $username, $hashedPassword, $email, $role, $created_at);

if ($stmt->execute()) {
    echo json_encode(["status" => "success"]);
} else {
    echo json_encode(["status" => "error", "message" => "Lỗi tạo tài khoản"]);
}
?>
