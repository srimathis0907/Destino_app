<?php
header('Content-Type: application/json');
require_once 'config.php';

$userId = $_POST['user_id'] ?? null;
$fullname = $_POST['fullname'] ?? null;
$username = $_POST['username'] ?? null;
$email = $_POST['email'] ?? null;
$phone = $_POST['phone'] ?? null;

if (!$userId || !$fullname || !$username || !$email) {
    echo json_encode(['status' => false, 'message' => 'Missing required fields.']);
    exit;
}

// --- START: NEW, STRICTER VALIDATION BLOCK ---
if (!preg_match("/^[a-zA-Z\s]+$/", $fullname)) {
     echo json_encode(['status' => false, 'message' => 'Name must contain only alphabets and spaces.']); exit;
}

if (!filter_var($email, FILTER_VALIDATE_EMAIL) || !preg_match('/@(gmail\.com|mail\.com)$/', $email)) {
     echo json_encode(['status' => false, 'message' => 'Only gmail.com and mail.com emails are accepted.']); exit;
}

if (!preg_match("/^[a-zA-Z0-9]+$/", $username)) {
     echo json_encode(['status' => false, 'message' => 'Invalid username format.']); exit;
}

if ($phone && !preg_match("/^\d{10}$/", $phone)) {
     echo json_encode(['status' => false, 'message' => 'Phone must be 10 digits.']); exit;
}
// --- END: NEW, STRICTER VALIDATION BLOCK ---


$profileImagePath = null;

if (isset($_FILES['profile_image'])) {
    $stmt_old = $conn->prepare("SELECT profile_image FROM users WHERE id = ?");
    $stmt_old->bind_param("i", $userId);
    $stmt_old->execute();
    $old_image_path = $stmt_old->get_result()->fetch_assoc()['profile_image'];
    $stmt_old->close();

    $upload_dir = 'uploads/profiles/';
    if (!file_exists($upload_dir)) { mkdir($upload_dir, 0777, true); }
    
    $file_name = uniqid('user_'.$userId.'_', true) . '.jpg';
    $target_path = $upload_dir . $file_name;

    if (move_uploaded_file($_FILES['profile_image']['tmp_name'], $target_path)) {
        $profileImagePath = $target_path;
        if ($old_image_path && file_exists($old_image_path)) {
            unlink($old_image_path);
        }
    }
}

if ($profileImagePath) {
    $sql = "UPDATE users SET fullname = ?, username = ?, email = ?, phone = ?, profile_image = ? WHERE id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("sssssi", $fullname, $username, $email, $phone, $profileImagePath, $userId);
} else {
    $sql = "UPDATE users SET fullname = ?, username = ?, email = ?, phone = ? WHERE id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("ssssi", $fullname, $username, $email, $phone, $userId);
}

if ($stmt->execute()) {
    echo json_encode(['status' => true, 'message' => 'Profile updated successfully.']);
} else {
    if ($conn->errno == 1062) {
        $errorMessage = $conn->error;
        if (strpos($errorMessage, 'email') !== false) {
            echo json_encode(['status' => false, 'message' => 'This email is already taken.']);
        } else if (strpos($errorMessage, 'username') !== false) {
            echo json_encode(['status' => false, 'message' => 'This username is already taken.']);
        } else {
            echo json_encode(['status' => false, 'message' => 'Update failed: Duplicate entry.']);
        }
    } else {
        echo json_encode(['status' => false, 'message' => 'Update failed: ' . $stmt->error]);
    }
}

$stmt->close();
$conn->close();
?>