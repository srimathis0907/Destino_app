<?php
header('Content-Type: application/json');
require_once 'config.php';
mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT); // Added for better error handling

// --- [UPDATED LOGIC START] ---
$userId = null;

// 1. Priority: Check URL Parameters (GET) - This fixes your Android error
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
if (!$userId || empty($userId)) {
    echo json_encode(['status' => false, 'message' => 'User ID is required.']);
    exit;
}
// --- [UPDATED LOGIC END] ---

// Your existing SQL query (unchanged)
$sql = "SELECT p.id, p.name, p.location, 
            (SELECT image_url FROM place_images WHERE place_id = p.id ORDER BY id ASC LIMIT 1) as image_url
        FROM places p
        JOIN saved_trips st ON p.id = st.place_id
        WHERE st.user_id = ?
        ORDER BY st.saved_at DESC";

try {
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $userId); // Assuming user ID is an integer 'i'
    $stmt->execute();
    $result = $stmt->get_result();
    $savedTrips = $result->fetch_all(MYSQLI_ASSOC);

    echo json_encode(['status' => true, 'data' => $savedTrips]);

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['status' => false, 'message' => 'Server Error: ' . $e->getMessage()]);
}

$stmt->close();
$conn->close();
?>