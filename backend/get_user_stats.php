<?php
header("Content-Type: application/json");
include 'config.php';

$response = array();
$admin_email = 'admin123@gmail.com'; // Admin email to exclude

// Query to get total users, EXCLUDING the admin
$total_query = "SELECT COUNT(id) AS total_users FROM users WHERE email != ?";
$stmt_total = $conn->prepare($total_query);
$stmt_total->bind_param("s", $admin_email);
$stmt_total->execute();
$total_users = $stmt_total->get_result()->fetch_assoc()['total_users'];

// Query to get active users, EXCLUDING the admin
$active_query = "SELECT COUNT(id) AS active_users FROM users WHERE status = 'active' AND email != ?";
$stmt_active = $conn->prepare($active_query);
$stmt_active->bind_param("s", $admin_email);
$stmt_active->execute();
$active_users = $stmt_active->get_result()->fetch_assoc()['active_users'];

// Query to get blocked users, EXCLUDING the admin
$blocked_query = "SELECT COUNT(id) AS blocked_users FROM users WHERE status = 'blocked' AND email != ?";
$stmt_blocked = $conn->prepare($blocked_query);
$stmt_blocked->bind_param("s", $admin_email);
$stmt_blocked->execute();
$blocked_users = $stmt_blocked->get_result()->fetch_assoc()['blocked_users'];

$response['error'] = false;
$response['total_users'] = $total_users;
$response['active_users'] = $active_users;
$response['blocked_users'] = $blocked_users;

echo json_encode($response);

$stmt_total->close();
$stmt_active->close();
$stmt_blocked->close();
$conn->close();
?>