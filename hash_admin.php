<?php
$password = 'admin123';
$hash = password_hash($password, PASSWORD_DEFAULT);
echo "Mật khẩu đã mã hóa: <br>" . $hash;
?>
