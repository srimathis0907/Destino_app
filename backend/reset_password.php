<?php
include 'config.php';

header('Content-Type: application/json');
$response = [];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $email = $_POST['email'] ?? '';
    $otp = $_POST['otp'] ?? '';
    $new_password = $_POST['new_password'] ?? '';

    if (empty($email) || empty($otp) || empty($new_password)) {
        http_response_code(400);
        echo json_encode(['status' => false, 'message' => 'Email, OTP, and new password are required.']);
        exit;
    }
    
    // Verify OTP
    $stmt = $conn->prepare("SELECT otp, otp_expires_at FROM users WHERE email = ?");
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $result = $stmt->get_result()->fetch_assoc();
    
    // Check if OTP matches and is not expired
    if ($result && $result['otp'] == $otp && time() < strtotime($result['otp_expires_at'])) {
        // OTP is valid, update the password
        $hashedPassword = password_hash($new_password, PASSWORD_DEFAULT);
        
        // Clear OTP fields and update password
        $updateStmt = $conn->prepare("UPDATE users SET password = ?, otp = NULL, otp_expires_at = NULL WHERE email = ?");
        $updateStmt->bind_param("ss", $hashedPassword, $email);
        
        if ($updateStmt->execute()) {
            $response = ['status' => true, 'message' => 'Password has been reset successfully.'];
        } else {
            http_response_code(500);
            $response = ['status' => false, 'message' => 'Failed to update password.'];
        }
    } else {
        http_response_code(400);
        $response = ['status' => false, 'message' => 'Invalid or expired OTP.'];
    }
    $stmt->close();
} else {
    http_response_code(405);
    $response = ['status' => false, 'message' => 'Invalid request method.'];
}

echo json_encode($response);
$conn->close();
?>