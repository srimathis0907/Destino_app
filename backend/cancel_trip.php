<?php
include 'config.php';

header('Content-Type: application/json');
$response = ['status' => false, 'message' => 'An unknown error occurred.'];

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    $response['message'] = 'Invalid request method.';
    echo json_encode($response);
    exit;
}

// In a real app, you would also verify the user ID from a secure session token
if (!isset($_POST['trip_id'])) {
    http_response_code(400);
    $response['message'] = 'Missing required parameter: trip_id is required.';
    echo json_encode($response);
    exit;
}

$tripId = (int)$_POST['trip_id'];

try {
    // We will update the status of the trip to 'cancelled'
    $sql = "UPDATE trips SET status = 'cancelled' WHERE id = ?";
            
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $tripId);
    
    if ($stmt->execute()) {
        if ($stmt->affected_rows > 0) {
            $response = ['status' => true, 'message' => 'Trip cancelled successfully!'];
            http_response_code(200);
        } else {
            $response['message'] = 'Trip not found.';
            http_response_code(404);
        }
    } else {
        $response['message'] = 'Failed to cancel trip.';
        http_response_code(500);
    }
    $stmt->close();

} catch (Exception $e) {
    http_response_code(500);
    $response['message'] = 'Database transaction failed: ' . $e->getMessage();
}

echo json_encode($response);
$conn->close();
?>