<?php
// Add error reporting for debugging
ini_set('display_errors', 1);
error_reporting(E_ALL);

header("Content-Type: application/json");
include 'config.php'; // Ensure this path is correct

$response = array("error" => true, "message" => "An error occurred fetching latest reviews."); // Default error

// Check DB connection
if (!isset($conn) || $conn->connect_error) {
     http_response_code(500); // Internal Server Error
     $response['message'] = 'Database connection error: ' . ($conn->connect_error ?? 'Unknown error');
     echo json_encode($response);
     exit;
}

// --- SQL QUERY FIXED ---
// 1. Changed p.place_name to p.name
// 2. Changed p.place_location to p.location
// 3. Added CONCAT for full_location
// 4. Added r.id just in case it's needed by the Android model
$sql = "SELECT
            r.id, 
            r.rating,
            r.review_text,
            u.fullname AS user_name,
            p.name AS place_name, 
            p.location AS place_location, 
            CONCAT(p.name, ', ', p.location) AS full_location,
            r.created_at
        FROM reviews r
        JOIN users u ON r.user_id = u.id
        JOIN places p ON r.place_id = p.id
        ORDER BY r.created_at DESC
        LIMIT 3";

$result = $conn->query($sql);

if ($result) {
    $reviews = array();
    while ($row = $result->fetch_assoc()) {
        // Ensure rating is a number (float) for consistency
        $row['rating'] = (float)$row['rating']; 
        $reviews[] = $row;
    }
    // Success response
    $response = array("error" => false, "message"=>"Latest reviews fetched.", "reviews" => $reviews);
    http_response_code(200);
} else {
    // SQL query failed
    http_response_code(500); 
    $response["message"] = "Could not fetch latest reviews. SQL Error: " . $conn->error;
}

echo json_encode($response);
$conn->close();
?>