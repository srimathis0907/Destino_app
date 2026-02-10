<?php
include 'config.php'; // Your database connection

header('Content-Type: application/json');

$response = [];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $email = $_POST['email'] ?? '';
    $password = $_POST['password'] ?? '';

    if (empty($email) || empty($password)) {
        http_response_code(400);
        $response['status'] = false;
        $response['message'] = 'Email and password are required.';
        echo json_encode($response);
        exit;
    }
    
    // --- START: STRICTER VALIDATION ---
    if (!filter_var($email, FILTER_VALIDATE_EMAIL) || !preg_match('/@(gmail\.com|mail\.com)$/', $email)) {
        http_response_code(400);
        $response['status'] = false;
        $response['message'] = 'Please use a valid gmail.com or mail.com address.';
        echo json_encode($response);
        exit;
    }
    // --- END: STRICTER VALIDATION ---

    // Updated Query: Now selecting 'status' as well
    $stmt = $conn->prepare("SELECT id, fullname, email, password, status FROM users WHERE email = ?");
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $stmt->store_result();

    if ($stmt->num_rows === 1) {
        // Updated bind_result to include $status
        $stmt->bind_result($id, $fullname, $db_email, $hashedPassword, $status);
        $stmt->fetch();

        if (password_verify($password, $hashedPassword)) {

            // --- NEW BLOCKING LOGIC START ---
            if ($status === 'blocked') { 
                $response['status'] = false; 
                $response['message'] = 'Account blocked. Please contact admin(srimathisivan09@gmail.com).';
                echo json_encode($response);
                exit(); // Stop the script immediately
            }
            // --- NEW BLOCKING LOGIC END ---

            $session_token = bin2hex(random_bytes(32));
            
            $updateStmt = $conn->prepare("UPDATE users SET session_token = ? WHERE id = ?");
            $updateStmt->bind_param("si", $session_token, $id);
            $updateStmt->execute();
            $updateStmt->close();

            $response['status'] = true;
            $response['message'] = 'Login successful.';
            $response['token'] = $session_token;
            $response['user'] = [
                'id' => $id,
                'fullname' => $fullname,
                'email' => $db_email 
            ];
        } else {
            $response['status'] = false;
            $response['message'] = 'Incorrect password.';
        }
    } else {
        $response['status'] = false;
        $response['message'] = 'Email not found.';
    }

    $stmt->close();
} else {
    $response['status'] = false;
    $response['message'] = 'Invalid request method.';
}

echo json_encode($response);
$conn->close();
?>