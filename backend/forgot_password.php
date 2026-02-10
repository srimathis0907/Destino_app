<?php
include 'config.php'; // Your database connection file
require 'vendor/autoload.php'; // Required for PHPMailer

use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

header('Content-Type: application/json');
$response = [];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $email = $_POST['email'] ?? '';

    if (empty($email)) {
        http_response_code(400);
        $response['status'] = false;
        $response['message'] = 'Email is required.';
        echo json_encode($response);
        exit;
    }

    // Check if user exists
    $stmt = $conn->prepare("SELECT id FROM users WHERE email = ?");
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 1) {
        // Generate a 6-digit OTP
        $otp = rand(100000, 999999);
        // Set OTP expiration time (e.g., 10 minutes from now)
        $otp_expires_at = date('Y-m-d H:i:s', strtotime('+10 minutes'));

        // Save OTP and its expiration to the database
        $updateStmt = $conn->prepare("UPDATE users SET otp = ?, otp_expires_at = ? WHERE email = ?");
        $updateStmt->bind_param("sss", $otp, $otp_expires_at, $email);
        $updateStmt->execute();

    
        // Send the OTP email using PHPMailer
        $mail = new PHPMailer(true);
        try {
            // --- IMPORTANT: CONFIGURE YOUR EMAIL SERVER SETTINGS HERE ---
            // Server settings (Enable verbose debug output for more info)
            // $mail->SMTPDebug = \PHPMailer\PHPMailer\SMTP::DEBUG_SERVER; // Uncomment for detailed logs
            $mail->isSMTP();
            $mail->Host       = 'smtp.gmail.com'; // Your SMTP host (e.g., Gmail)
            $mail->SMTPAuth   = true;
            $mail->Username   = 'srimathisivan09@gmail.com'; // Your full Gmail address
            $mail->Password   = 'jhgq oyzi phnp ppos'; // Paste your App Password here

            // --- THIS IS THE CORRECTED LINE ---
            $mail->SMTPSecure = PHPMailer::ENCRYPTION_SMTPS;
            
            $mail->Port       = 465;

            // Recipients
            $mail->setFrom('srimathisivan09@gmail.com', 'Weekend App Support');
            $mail->addAddress($email);

            // Email Content
            $mail->isHTML(true);
            $mail->Subject = 'Your Password Reset OTP';
            $mail->Body    = "Your one-time password (OTP) to reset your password is: <b>$otp</b><br>This OTP is valid for 10 minutes.";

            $mail->send();
            $response = ['status' => true, 'message' => 'An OTP has been sent to your email.'];

        } catch (Exception $e) {
            // --- START: UPDATED CATCH BLOCK ---
            http_response_code(500); // Internal Server Error
            // Log the detailed error message from PHPMailer
            $detailed_error = "Mailer Error: " . $mail->ErrorInfo . " | PHP Exception: " . $e->getMessage();
            error_log($detailed_error); // Log to PHP error log
            $response = ['status' => false, 'message' => "OTP could not be sent. Error: " . $detailed_error];
            // --- END: UPDATED CATCH BLOCK ---
        }
    } else {
        $response = ['status' => false, 'message' => 'Email not found in our records.'];
    }
    $stmt->close();
} else {
    $response = ['status' => false, 'message' => 'Invalid request method.'];
}

echo json_encode($response);
$conn->close();
?>