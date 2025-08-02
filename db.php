<?php
$host = "localhost";
$user = "root";
$pass = "";
$db = "mydata_dht11"; // hoặc tên đúng của database bạn tạo

$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    die("Kết nối thất bại: " . $conn->connect_error);
}
?>
