<?php
header('Content-Type: application/json');
require_once 'config.php';
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
$userId = $data['user_id'] ?? null;

if (!$userId || empty($userId)) { // Check for null or empty string
    echo json_encode(['status' => false, 'message' => 'User ID is required.']);
    exit;
}
// --- [END MODIFICATION] ---

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