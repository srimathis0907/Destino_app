<?php
header('Content-Type: application/json');
require_once 'config.php'; // Your database connection
mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT); // Added for better error handling

// --- [NEW LOGIC START] ---
// Read data from POST (form-data)
$data = $_POST;

// If $_POST is empty, check for raw JSON input (from Postman/Retrofit)
if (empty($data)) {
    $rawInput = file_get_contents('php://input');
    $jsonData = json_decode($rawInput, true); // Decode as an associative array
    
    if (json_last_error() === JSON_ERROR_NONE && is_array($jsonData)) {
        $data = $jsonData;
    }
}
// --- [NEW LOGIC END] ---

// [MODIFIED] Check the $data array instead of $_GET
if (!isset($data['user_id']) || empty($data['user_id'])) {
    echo json_encode(['status' => false, 'message' => 'User ID is required.']);
    exit;
}
$userId = $data['user_id'];
// --- [END MODIFICATION] ---


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