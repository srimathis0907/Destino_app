<?php
header('Content-Type: application/json');
require_once 'config.php';

// Decode JSON input from the app
$data = json_decode(file_get_contents('php://input'), true);

$userId = $data['user_id'] ?? null;
$currentPassword = $data['current_password'] ?? null;
$newPassword = $data['new_password'] ?? null;

if (!$userId || !$currentPassword || !$newPassword) {
    echo json_encode(['status' => false, 'message' => 'All fields are required.']);
    exit;
}

// --- START: NEW PASSWORD VALIDATION ---
$passwordRegex = '/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{6,8}$/';
if (!preg_match($passwordRegex, $newPassword)) {
    echo json_encode(['status' => false, 'message' => 'New password must be 6-8 chars with uppercase, lowercase, number, & special char.']);
    exit;
}
// --- END: NEW PASSWORD VALIDATION ---


// 1. Fetch the user's current hashed password from the database
$stmt = $conn->prepare("SELECT password FROM users WHERE id = ?");
$stmt->bind_param("i", $userId);
$stmt->execute();
$result = $stmt->get_result();
$user = $result->fetch_assoc();
$stmt->close();

if (!$user) {
    echo json_encode(['status' => false, 'message' => 'User not found.']);
    exit;
}

// 2. Verify if the provided current password matches the stored hash
if (password_verify($currentPassword, $user['password'])) {
    // 3. If correct, hash the new password
    $newHashedPassword = password_hash($newPassword, PASSWORD_DEFAULT);

    // 4. Update the database with the new hashed password
    $update_stmt = $conn->prepare("UPDATE users SET password = ? WHERE id = ?");
    $update_stmt->bind_param("si", $newHashedPassword, $userId);
    
    if ($update_stmt->execute()) {
        echo json_encode(['status' => true, 'message' => 'Password updated successfully.']);
    } else {
        echo json_encode(['status' => false, 'message' => 'Failed to update password.']);
    }
    $update_stmt->close();
} else {
    // If the current password is incorrect
    echo json_encode(['status' => false, 'message' => 'Incorrect current password.']);
}

$conn->close();
?>