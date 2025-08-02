<?php
header('Content-Type: application/json');

// Cấu hình gửi mail
use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

// Nạp thư viện PHPMailer
require 'PHPMailer/src/Exception.php';
require 'PHPMailer/src/PHPMailer.php';
require 'PHPMailer/src/SMTP.php';

// Kết nối cơ sở dữ liệu
$conn = new mysqli("localhost", "root", "", "mydata_dht11");
if ($conn->connect_error) {
    echo json_encode(["status" => "error", "message" => "Lỗi kết nối CSDL"]);
    exit();
}

// Nhận dữ liệu từ app
$data = json_decode(file_get_contents("php://input"), true);
$email = $data["email"] ?? null;
$username= $data["username"] ?? null;

if (!$email || !$username) {
    echo json_encode(["status" => "error", "message" => "Thiếu email hoac ten dang nhap "]);
    exit();
}

// Kiểm tra email có tồn tại không
$stmt = $conn->prepare("SELECT id FROM users WHERE email = ? AND username = ?");
$stmt->bind_param("ss", $email, $username);
$stmt->execute();
$stmt->store_result();

if ($stmt->num_rows === 0) {
    echo json_encode(["status" => "error", "message" => "Khong tim thay email va user"]);
    exit();
}

// Tạo mật khẩu mới
$newPassword = substr(str_shuffle("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"), 0, 8);
$hashedPassword = password_hash($newPassword, PASSWORD_DEFAULT);

// Cập nhật mật khẩu mới
$stmt = $conn->prepare("UPDATE users SET password = ? WHERE email = ? AND username = ?");
$stmt->bind_param("sss", $hashedPassword, $email,$username);
$stmt->execute();

// Gửi email với mật khẩu mới
$mail = new PHPMailer(true);
try {
    // Cấu hình SMTP
    $mail->isSMTP();
    $mail->Host = 'smtp.gmail.com';      // 🔁 Thay bằng SMTP của bạn
    $mail->SMTPAuth = true;
    $mail->Username = 'duongtran11092003@gmail.com';  // 🔁 Email gửi đi
    $mail->Password = 'cayw dxgd wvwm azen';     // 🔁 App password
    $mail->SMTPSecure = 'tls';
    $mail->Port = 587;

    // Thiết lập nội dung email
    $mail->setFrom('duongtran11092003@gmail.com', 'Support Team');
    $mail->addAddress($email);
    $mail->Subject = 'Yeu cau dat lai mat khau';
    $mail->Body = "Xin chào $username,\n\nMật khẩu mới của bạn là: $newPassword\n\nVui lòng đăng nhập và đổi lại mật khẩu.";

    $mail->send();
    echo json_encode(["status" => "success", "message" => "Mật khẩu mới đã được gửi tới email"]);
} catch (Exception $e) {
    echo json_encode(["status" => "error", "message" => "Không gửi được email: " . $mail->ErrorInfo]);
}
?>
