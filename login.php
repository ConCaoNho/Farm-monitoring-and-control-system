<?php
require 'db.php';

$data = json_decode(file_get_contents("php://input"), true);
$username = $data['username'];
$password = $data['password'];

$sql = "SELECT * FROM users WHERE username = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $username);
$stmt->execute();
$result = $stmt->get_result();

if ($row = $result->fetch_assoc()) {
    if (password_verify($password, $row['password'])) {
        echo json_encode([
            "status" => "success",
            "role" => $row['role'],
            "message" => "Đăng nhập thành công"
        ]);
    } else {
        echo json_encode(["status" => "error", "message" => "Sai mật khẩu"]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Tài khoản không tồn tại"]);
}
