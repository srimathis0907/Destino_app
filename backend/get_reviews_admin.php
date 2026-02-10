<?php
// Add error reporting for debugging
ini_set('display_errors', 1);
error_reporting(E_ALL);

header("Content-Type: application/json");
include 'config.php'; // Make sure this path is correct

$response = array("error" => true, "message" => "An error occurred fetching reviews."); // Default error

// Check DB connection
if (!isset($conn) || $conn->connect_error) {
     http_response_code(500);
     $response['message'] = 'Database connection error: ' . ($conn->connect_error ?? 'Unknown error');
     echo json_encode($response);
     exit;
}

// Use COALESCE to handle potential NULL location
$sql = "SELECT
            r.id,
            r.rating,
            r.review_text,
            r.created_at,
            u.fullname AS user_name,
            p.name AS place_name, 
            -- FIX: Use COALESCE to provide an empty string if location is NULL
            CONCAT(p.name, ', ', COALESCE(p.location, '')) AS full_location 
        FROM reviews r
        JOIN users u ON r.user_id = u.id
        JOIN places p ON r.place_id = p.id
        ORDER BY r.created_at DESC";

$result = $conn->query($sql);

if ($result) {
    $reviews = array();
    while($row = $result->fetch_assoc()) {
        $row['rating'] = (float)$row['rating']; 
        // Trim trailing comma and space if location was NULL
        $row['full_location'] = rtrim($row['full_location'], ', '); 
        $reviews[] = $row;
    }
    $response = array("error" => false, "message"=>"Admin reviews fetched.", "reviews" => $reviews);
     http_response_code(200);
} else {
    http_response_code(500); 
    $response["message"] = "Could not fetch reviews. SQL Error: " . $conn->error;
}

echo json_encode($response);
$conn->close();
?>