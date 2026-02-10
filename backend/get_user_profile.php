<?php
header('Content-Type: application/json');
require_once 'config.php'; // Your database connection
mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT); // Added for better error handling

// --- [UPDATED LOGIC START] ---
// Initialize userId as null
$userId = null;

// 1. Priority: Check URL Parameters (GET) - This fixes your specific Android error
if (isset($_GET['user_id'])) {
    $userId = $_GET['user_id'];
} 
// 2. Check POST (Form Data)
elseif (isset($_POST['user_id'])) {
    $userId = $_POST['user_id'];
}
// 3. Check Raw JSON (Postman/Retrofit Body)
else {
    $rawInput = file_get_contents('php://input');
    $jsonData = json_decode($rawInput, true);
    
    if (json_last_error() === JSON_ERROR_NONE && isset($jsonData['user_id'])) {
        $userId = $jsonData['user_id'];
    }
}

// Validate if we successfully found an ID
if (empty($userId)) {
    echo json_encode(['status' => false, 'message' => 'User ID is required.']);
    exit;
}
// --- [UPDATED LOGIC END] ---


// Make sure your table and column names match
// The image from your other project (image_1a0b1b.png) is for 'foodstall'
// This 'destino' project uses a 'users' table
$sql = "SELECT id, fullname, username, email, phone, profile_image FROM users WHERE id = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $userId); // Assuming user ID is an integer 'i'
$stmt->execute();
$result = $stmt->get_result();

if ($user = $result->fetch_assoc()) {
    echo json_encode(['status' => true, 'data' => $user]);
} else {
    echo json_encode(['status' => false, 'message' => 'User not found.']);
}

$stmt->close();
$conn->close();
?>