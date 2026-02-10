<?php
header("Content-Type: application/json");
include 'config.php'; // Ensure this path is correct

$response = array('error' => true, 'message' => 'An error occurred.'); // Default error

if (!isset($_GET['user_id']) || !is_numeric($_GET['user_id'])) {
     http_response_code(400); // Bad Request
    $response['message'] = "Valid User ID is required.";
    echo json_encode($response);
    exit;
}

$user_id = intval($_GET['user_id']);

try {
    // 1. Future: Status is 'future' AND starts strictly after today for this user
    $future_q = "SELECT COUNT(id) AS count FROM trips WHERE user_id = ? AND LOWER(status) = 'future' AND start_date > CURDATE()";
    $stmt_f = $conn->prepare($future_q);
    $stmt_f->bind_param("i", $user_id);
    $stmt_f->execute();
    $future_count = (int)$stmt_f->get_result()->fetch_assoc()['count'];
    $stmt_f->close();

    // 2. Ongoing (Active): Status is 'active' OR (Status is 'future' but date range includes today) for this user
    $ongoing_q = "SELECT COUNT(id) AS count FROM trips 
                  WHERE user_id = ? 
                  AND (
                      LOWER(status) = 'active' 
                      OR 
                      (LOWER(status) = 'future' AND CURDATE() BETWEEN start_date AND end_date)
                  )
                  AND LOWER(status) NOT IN ('cancelled', 'finished', 'completed')";
    $stmt_o = $conn->prepare($ongoing_q);
    $stmt_o->bind_param("i", $user_id);
    $stmt_o->execute();
    $ongoing_count = (int)$stmt_o->get_result()->fetch_assoc()['count'];
    $stmt_o->close();

    // 3. Completed: Status is 'finished' (or 'completed') for this user
    $completed_q = "SELECT COUNT(id) AS count FROM trips WHERE user_id = ? AND (LOWER(status) = 'finished' OR LOWER(status) = 'completed')";
    $stmt_c = $conn->prepare($completed_q);
    $stmt_c->bind_param("i", $user_id);
    $stmt_c->execute();
    $completed_count = (int)$stmt_c->get_result()->fetch_assoc()['count'];
    $stmt_c->close();

    // 4. Cancelled: Status is 'cancelled' for this user
    $cancelled_q = "SELECT COUNT(id) AS count FROM trips WHERE user_id = ? AND LOWER(status) = 'cancelled'";
    $stmt_x = $conn->prepare($cancelled_q);
    $stmt_x->bind_param("i", $user_id);
    $stmt_x->execute();
    $cancelled_count = (int)$stmt_x->get_result()->fetch_assoc()['count'];
    $stmt_x->close();

    // Success response
    $response['error'] = false;
    $response['message'] = 'User trip counts fetched successfully.'; // Added message
    $response['future_trips'] = $future_count; // ADDED THIS FIELD
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