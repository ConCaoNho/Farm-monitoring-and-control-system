<?php
header('Content-Type: text/plain'); // Set response type

$hostname = "localhost"; 
$username = "root"; 
$password = ""; 
$database = "mydata_dht11"; 

$conn = mysqli_connect($hostname, $username, $password, $database);

if (!$conn) { 
    die("Connection failed: " . mysqli_connect_error()); 
} 

if(isset($_POST["temperature"]) && isset($_POST["humidity"])) {
    $t = (float)$_POST["temperature"]; // Force numeric type
    $h = (float)$_POST["humidity"];

    // Use prepared statements to prevent SQL injection
    $sql = "INSERT INTO dht11 (Temperature, Humidity) VALUES (?, ?)";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("dd", $t, $h); // 'dd' = double/double

    if ($stmt->execute()) { 
        echo "New record created successfully";
    } else { 
        echo "Error: " . $stmt->error; 
    }
    $stmt->close();
} else {
    echo "Error: Missing temperature or humidity data";
}

$conn->close();
?>
