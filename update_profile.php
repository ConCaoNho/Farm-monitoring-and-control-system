<?php
header("Content-Type: application/json; charset=UTF-8");
$conn = new mysqli("localhost", "root", "", "mydata_dht11");
if ($conn->connect_error) {
    echo json_encode(["status" => "error", "message" => "Lỗi kết nối CSDL"]); exit();
}
$data = json_decode(file_get_contents("php://input"), true);
$current = $data["current_username"];
$oldPass = $data["old_password"];
$newUser = $data["new_username"];
$newPass = $data["new_password"];

$result = $conn->prepare("SELECT password FROM users WHERE username = ?");
$result->bind_param("s", $current);
$result->execute();
$res = $result->get_result();

if ($res->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "Tài khoản không tồn tại"]); exit();
}

$row = $res->fetch_assoc();
if (!password_verify($oldPass, $row["password"])) {
    echo json_encode(["status" => "error", "message" => "Sai mật khẩu cũ"]); exit();
}

// Hash mật khẩu mới
$hashed = password_hash($newPass, PASSWORD_BCRYPT);
$update = $conn->prepare("UPDATE users SET username = ?, password = ? WHERE username = ?");
$update->bind_param("sss", $newUser, $hashed, $current);

if ($update->execute()) {
    echo json_encode(["status" => "success", "message" => "Cập nhật thành công"]);
} else {
    echo json_encode(["status" => "error", "message" => "Lỗi khi cập nhật"]);
}
$conn->close();
?>
