<?php
// --- FOR DEBUGGING ---
// This will force PHP to show us the error message in the Logcat.
// We can turn this off later.
ini_set('display_errors', 1);
error_reporting(E_ALL);
// --- END DEBUGGING ---

header('Content-Type: application/json');

// --- THE FIX IS HERE ---
// Changed 'db.php' to 'config.php' to match your other files.
include 'config.php'; 
// --- END OF FIX ---

// Check for a valid database connection
if (!isset($conn) || $conn->connect_error) {
    echo json_encode([
        'status' => false, 
        'message' => 'Database connection failed. Check config.php. Error: ' . $conn->connect_error
    ]);
    exit;
}

// Read the JSON payload from the request
$data = json_decode(file_get_contents('php://input'), true);

if (!$data) {
    echo json_encode(['status' => false, 'message' => 'Invalid JSON payload received.']);
    exit;
}

// Get the individual data fields from the JSON
$user_id = $data['user_id'] ?? null;
$place_id = $data['place_id'] ?? null;
$trip_id = $data['trip_id'] ?? null;
$category = $data['category'] ?? 'Trip'; 
$rating = $data['rating'] ?? 0;
$review_text = $data['review_text'] ?? '';

// Basic validation
if (empty($user_id) || empty($place_id) || empty($trip_id) || $rating == 0) {
    echo json_encode([
        'status' => false, 
        'message' => 'Missing required fields (user_id, place_id, trip_id, or rating).'
    ]);
    exit;
}

// Using MySQLi prepared statements to match your get_reviews_admin.php
$sql = "INSERT INTO reviews (user_id, place_id, trip_id, category, rating, review_text) VALUES (?, ?, ?, ?, ?, ?)";
$stmt = $conn->prepare($sql);

if ($stmt === false) {
    echo json_encode(['status' => false, 'message' => 'Database prepare statement failed: ' . $conn->error]);
    exit;
}

// Bind parameters: i = integer, s = string, d = double (for rating)
$stmt->bind_param("iiisds", $user_id, $place_id, $trip_id, $category, $rating, $review_text);

if ($stmt->execute()) {
    echo json_encode(['status' => true, 'message' => 'Review submitted successfully']);
} else {
    echo json_encode(['status' => false, 'message' => 'Database execute failed: ' . $stmt->error]);
}

$stmt->close();
$conn->close();