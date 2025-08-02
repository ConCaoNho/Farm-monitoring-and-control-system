<?php
header('Content-Type: application/json');

// Chỉ chấp nhận POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(["status" => "error", "message" => "Chỉ chấp nhận phương thức POST"]);
    exit;
}

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['id']) || !isset($data['username']) || !isset($data['role']) || !isset($data['requester'])) {
    echo json_encode(["status" => "error", "message" => "Thiếu tham số"]);
    exit;
}

$id = $data['id'];
$username = $data['username'];
$role = $data['role'];
$requester = $data['requester'];

// Không cho xoá admin gốc
if ($username === 'admin') {
    echo json_encode(["status" => "error", "message" => "Không thể xoá admin gốc"]);
    exit;
}

// Chỉ admin gốc mới được xoá admin khác
if ($role === 'admin' && $requester !== 'admin') {
    echo json_encode(["status" => "error", "message" => "Chỉ admin gốc mới có quyền xoá admin khác"]);
    exit;
}

$conn = new mysqli("localhost", "root", "", "mydata_dht11");
if ($conn->connect_error) {
    echo json_encode(["status" => "error", "message" => "Lỗi kết nối cơ sở dữ liệu"]);
    exit;
}

$stmt = $conn->prepare("DELETE FROM users WHERE id = ?");
$stmt->bind_param("i", $id);

if ($stmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Xoá tài khoản thành công"]);
} else {
    echo json_encode(["status" => "error", "message" => "Xoá tài khoản thất bại"]);
}

$stmt->close();
$conn->close();
?>
