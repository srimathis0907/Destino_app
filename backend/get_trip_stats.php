<?php
header("Content-Type: application/json");
include 'config.php'; // Ensure this path is correct

$response = array('error' => true, 'message' => 'Failed to calculate stats.'); // Default error response

try {
    // 1. Future: Status is 'future' AND starts strictly after today
    $future_q = "SELECT COUNT(id) AS count FROM trips WHERE LOWER(status) = 'future' AND start_date > CURDATE()";
    $future_result = $conn->query($future_q);
    $future_count = $future_result ? (int)$future_result->fetch_assoc()['count'] : 0;

    // 2. Ongoing (Active): Status is 'active' OR (Status is 'future' but date range includes today)
    //    We exclude cancelled and finished statuses explicitly to be safe.
    $ongoing_q = "SELECT COUNT(id) AS count FROM trips 
                  WHERE (
                      LOWER(status) = 'active' 
                      OR 
                      (LOWER(status) = 'future' AND CURDATE() BETWEEN start_date AND end_date)
                  )
                  AND LOWER(status) NOT IN ('cancelled', 'finished', 'completed')"; // Be explicit
    $ongoing_result = $conn->query($ongoing_q);
    $ongoing_count = $ongoing_result ? (int)$ongoing_result->fetch_assoc()['count'] : 0;

    // 3. Completed: Status is explicitly 'finished' (or 'completed' for backward compatibility)
    $completed_q = "SELECT COUNT(id) AS count FROM trips WHERE LOWER(status) = 'finished' OR LOWER(status) = 'completed'";
    $completed_result = $conn->query($completed_q);
    $completed_count = $completed_result ? (int)$completed_result->fetch_assoc()['count'] : 0;

    // 4. Cancelled: Status is explicitly 'cancelled'
    $cancelled_q = "SELECT COUNT(id) AS count FROM trips WHERE LOWER(status) = 'cancelled'";
    $cancelled_result = $conn->query($cancelled_q);
    $cancelled_count = $cancelled_result ? (int)$cancelled_result->fetch_assoc()['count'] : 0;

    // Success response
    $response['error'] = false;
    $response['message'] = 'Trip stats fetched successfully.'; // Added message
    $response['future_trips'] = $future_count; // Renamed from upcoming_trips for consistency
    $response['ongoing_trips'] = $ongoing_count; // Kept the same name
    $response['completed_trips'] = $completed_count; // Kept the same name
    $response['cancelled_trips'] = $cancelled_count; // Kept the same name
    
    http_response_code(200); // Set success code

} catch (Exception $e) {
    http_response_code(500); // Internal Server Error
    $response['message'] = 'Database query failed: ' . $e->getMessage();
}

echo json_encode($response);
$conn->close();
?>