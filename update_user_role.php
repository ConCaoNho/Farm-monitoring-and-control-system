<?php
header("Content-Type: application/json");

// Chỉ chấp nhận POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(["status" => "error", "message" => "Chỉ chấp nhận phương thức POST"]);
    exit;
}

// Kết nối database
$conn = new mysqli("localhost", "root", "", "mydata_dht11");
if ($conn->connect_error) {
    echo json_encode(["status" => "error", "message" => "Kết nối database thất bại"]);
    exit;
}

// Lấy dữ liệu JSON từ body
$input = json_decode(file_get_contents("php://input"), true);
$id = $input['id'] ?? null;
$newRole = $input['role'] ?? null;

// Kiểm tra dữ liệu đầu vào
if (!$id || !$newRole) {
    echo json_encode(["status" => "error", "message" => "Thiếu dữ liệu id hoặc role"]);
    exit;
}

// Ngăn thay đổi quyền của admin gốc (id = 0 hoặc username = 'admin')
if ($id == 0) {
    echo json_encode(["status" => "error", "message" => "Không thể thay đổi quyền admin gốc"]);
    exit;
}

// Cập nhật quyền trong bảng users
$stmt = $conn->prepare("UPDATE users SET role = ? WHERE id = ?");
$stmt->bind_param("si", $newRole, $id);

if ($stmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Cập nhật quyền thành công"]);
} else {
    echo json_encode(["status" => "error", "message" => "Cập nhật quyền thất bại"]);
}

$stmt->close();
$conn->close();
?>
