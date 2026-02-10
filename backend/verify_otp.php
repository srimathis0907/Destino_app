<?php
include 'config.php'; // Your database connection

header('Content-Type: application/json');
$response = [];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $email = $_POST['email'] ?? '';
    $otp = $_POST['otp'] ?? '';

    if (empty($email) || empty($otp)) {
        http_response_code(400);
        $response = ['status' => false, 'message' => 'Email and OTP are required.'];
        echo json_encode($response);
        exit;
    }

    try {
        $stmt = $conn->prepare("SELECT otp, otp_expires_at FROM users WHERE email = ?");
        $stmt->bind_param("s", $email);
        $stmt->execute();
        $result = $stmt->get_result()->fetch_assoc();

        if ($result && $result['otp'] == $otp && time() < strtotime($result['otp_expires_at'])) {
            // OTP is correct and not expired
            $response = ['status' => true, 'message' => 'OTP Verified successfully.'];
        } else {
            // OTP is wrong or has expired
            http_response_code(400);
            $response = ['status' => false, 'message' => 'Invalid or expired OTP.'];
        }
        $stmt->close();

    } catch (Exception $e) {
        http_response_code(500);
        $response = ['status' => false, 'message' => 'Database error: ' . $e->getMessage()];
    }
} else {
    http_response_code(405);
    $response = ['status' => false, 'message' => 'Invalid request method.'];
}

echo json_encode($response);
$conn->close();
?>