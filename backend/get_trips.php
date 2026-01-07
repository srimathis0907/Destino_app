<?php
include 'config.php';
header('Content-Type: application/json');

$response = ['status' => false, 'message' => 'An error occurred.'];

if (!isset($_GET['user_id']) || !isset($_GET['status'])) {
    http_response_code(400);
    $response['message'] = 'user_id and status are required.';
    echo json_encode($response);
    exit;
}

$userId = intval($_GET['user_id']);
$status = strtolower(trim($_GET['status'])); // Ensure status is lowercase

try {
    // Determine query based on requested status.
    if ($status === 'future') {
        // --- FIX HERE: Include 'active' trips in the 'future' list ---
        // Get trips starting today or later with status 'future' OR trips with status 'active'.
        // Exclude finished/cancelled explicitly.
        $sql = "SELECT t.*, 
                       (SELECT pi.image_url FROM place_images pi WHERE pi.place_id = t.place_id LIMIT 1) as place_image
                FROM trips t
                WHERE t.user_id = ?
                  AND (
                       (LOWER(t.status) = 'future' AND t.start_date >= CURDATE())
                       OR 
                       (LOWER(t.status) = 'active') 
                      )
                  AND LOWER(t.status) NOT IN ('finished', 'completed', 'cancelled')
                ORDER BY t.start_date ASC";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("i", $userId);

    } elseif ($status === 'ongoing' || $status === 'active') { // This specific case might be less needed now
        // Get only trips explicitly marked as 'active' and within date range
        $sql = "SELECT t.*, (SELECT pi.image_url FROM place_images pi WHERE pi.place_id = t.place_id LIMIT 1) as place_image
                FROM trips t
                WHERE t.user_id = ?
                  AND CURDATE() BETWEEN t.start_date AND t.end_date
                  AND LOWER(t.status) = 'active'
                ORDER BY t.start_date ASC";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("i", $userId);

    } elseif ($status === 'completed' || $status === 'finished') {
        // Fetch 'finished' status directly
        $sql = "SELECT t.*, (SELECT pi.image_url FROM place_images pi WHERE pi.place_id = t.place_id LIMIT 1) as place_image
                FROM trips t
                WHERE t.user_id = ?
                  AND LOWER(t.status) IN ('finished', 'completed')
                ORDER BY t.end_date DESC";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("i", $userId);

    } elseif ($status === 'cancelled' || $status === 'canceled') {
        // Fetch 'cancelled' status directly
        $sql = "SELECT t.*, (SELECT pi.image_url FROM place_images pi WHERE pi.place_id = t.place_id LIMIT 1) as place_image
                FROM trips t
                WHERE t.user_id = ?
                  AND LOWER(t.status) = 'cancelled'
                ORDER BY t.start_date DESC";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("i", $userId);

    } else {
        // Fallback should ideally not be hit if app uses defined statuses
        http_response_code(400);
        $response['message'] = 'Invalid status requested.';
        echo json_encode($response);
        exit;
    }

    $stmt->execute();
    $result = $stmt->get_result();
    $trips = [];
    while ($row = $result->fetch_assoc()) {
        // Add 'is_today' flag
        $row['is_today'] = ($row['start_date'] == date('Y-m-d'));
        
        $row['status'] = strtolower($row['status']); // Ensure status is lowercase
        $trips[] = $row;
    }
    $stmt->close();
    
    $response = ['status' => true, 'message' => 'Trips fetched.', 'data' => $trips];
    http_response_code(200);

} catch (Exception $e) {
    http_response_code(500);
    $response['message'] = 'Database query failed: ' . $e->getMessage();
}

echo json_encode($response);
$conn->close();
?>