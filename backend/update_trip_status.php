<?php
include 'config.php';
header('Content-Type: application/json');

$response = ['status' => false, 'message' => 'An error occurred.'];

if ($_SERVER['REQUEST_METHOD'] !== 'POST' || !isset($_POST['trip_id']) || !isset($_POST['status'])) {
    http_response_code(400);
    $response['message'] = 'trip_id and status are required.';
    echo json_encode($response);
    exit;
}

$tripId = intval($_POST['trip_id']);
$newStatus = $_POST['status'];
$allowed_statuses = ['active', 'cancelled', 'completed']; // Define allowed statuses

if (!in_array($newStatus, $allowed_statuses)) {
    http_response_code(400);
    $response['message'] = 'Invalid status provided.';
    echo json_encode($response);
    exit;
}

try {
    $sql = "UPDATE trips SET status = ? WHERE id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("si", $newStatus, $tripId);
    
    if ($stmt->execute()) {
        if ($stmt->affected_rows > 0) {
            $response = ['status' => true, 'message' => 'Trip status updated successfully.'];
            http_response_code(200);
        } else {
            $response['message'] = 'Trip not found or status is already updated.';
            http_response_code(404);
        }
    } else {
        throw new Exception("Execute failed: " . $stmt->error);
    }
    $stmt->close();

} catch (Exception $e) {
    http_response_code(500);
    $response['message'] = 'Database update failed: ' . $e->getMessage();
}

echo json_encode($response);
$conn->close();
?>