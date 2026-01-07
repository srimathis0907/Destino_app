<?php
header("Content-Type: application/json");
include 'config.php';

// CORRECTED: Added a WHERE clause to exclude the admin email address
$sql = "SELECT id, fullname, email, status FROM users WHERE email != 'admin123@gmail.com' ORDER BY created_at DESC";

$result = $conn->query($sql);

if ($result) {
    $users = array();
    while($row = $result->fetch_assoc()) {
        $users[] = $row;
    }
    echo json_encode(array("error" => false, "users" => $users));
} else {
    echo json_encode(array("error" => true, "message" => "Could not fetch users."));
}

$conn->close();
?>