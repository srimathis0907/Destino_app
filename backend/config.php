<?php
$host = "localhost";     // XAMPP default host
$user = "root";          // Default username in XAMPP
$password = "12345";          // No password in XAMPP by default
$dbname = "weekend_holiday";     // Your database name

// Create connection
$conn = new mysqli($host, $user, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    die("❌ Connection failed: " . $conn->connect_error);
}

// Optional: Set character set
$conn->set_charset("utf8");

?>
