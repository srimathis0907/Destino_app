<?php
include 'config.php';

header('Content-Type: application/json');

$response = [];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $fullname = $_POST['fullname'] ?? '';
    $username = $_POST['username'] ?? '';
    $email = $_POST['email'] ?? '';
    $password = $_POST['password'] ?? '';

    // --- START: NEW, STRICTER VALIDATION BLOCK ---

    // 1. Basic empty check
    if (empty($fullname) || empty($username) || empty($email) || empty($password)) {
        $response['status'] = false;
        $response['message'] = 'All fields are required.';
        echo json_encode($response);
        exit;
    }

    // 2. New Name Validation (Only letters and spaces)
    if (!preg_match("/^[a-zA-Z\s]+$/", $fullname)) {
        $response['status'] = false;
        $response['message'] = 'Name must contain only alphabets and spaces.';
        echo json_encode($response);
        exit;
    }

    // 3. New, Stricter Email Validation (Only gmail.com or mail.com)
    if (!filter_var($email, FILTER_VALIDATE_EMAIL) || !preg_match('/@(gmail\.com|mail\.com)$/', $email)) {
        $response['status'] = false;
        $response['message'] = 'Only gmail.com and mail.com emails are accepted.';
        echo json_encode($response);
        exit;
    }

    // 4. Stricter Password Validation
    $passwordRegex = '/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{6,8}$/';
    if (!preg_match($passwordRegex, $password)) {
        $response['status'] = false;
        $response['message'] = 'Password must be 6-8 characters and include uppercase, lowercase, number, and special character.';
        echo json_encode($response);
        exit;
    }
    // --- END: NEW, STRICTER VALIDATION BLOCK ---

    // Check if username already exists
    $stmt_user = $conn->prepare("SELECT id FROM users WHERE username = ?");
    $stmt_user->bind_param("s", $username);
    $stmt_user->execute();
    $stmt_user->store_result();
    
    if ($stmt_user->num_rows > 0) {
        $response['status'] = false;
        $response['message'] = 'Username already exists. Please choose another one.';
        echo json_encode($response);
        exit;
    }
    $stmt_user->close();

    // Check if email already exists
    $stmt_email = $conn->prepare("SELECT id FROM users WHERE email = ?");
    $stmt_email->bind_param("s", $email);
    $stmt_email->execute();
    $stmt_email->store_result();
    
    if ($stmt_email->num_rows > 0) {
        $response['status'] = false;
        $response['message'] = 'Email already exists.';
        echo json_encode($response);
        exit;
    }
    $stmt_email->close();

    // Hash password
    $hashedPassword = password_hash($password, PASSWORD_DEFAULT);

    // Insert user
    $stmt_insert = $conn->prepare("INSERT INTO users (fullname, username, email, password) VALUES (?, ?, ?, ?)");
    $stmt_insert->bind_param("ssss", $fullname, $username, $email, $hashedPassword);

    if ($stmt_insert->execute()) {
        $response['status'] = true;
        $response['message'] = 'Signup successful.';
        $response['user_id'] = $stmt_insert->insert_id;
    } else {
        $response['status'] = false;
        $response['message'] = 'Signup failed: ' . $stmt_insert->error;
    }
    $stmt_insert->close();
} else {
    $response['status'] = false;
    $response['message'] = 'Invalid request method.';
}

echo json_encode($response);
?>