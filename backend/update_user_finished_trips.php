<?php
include 'config.php';
header('Content-Type: application/json');

$response = ['status' => false, 'message' => 'An error occurred.'];

if ($_SERVER['REQUEST_METHOD'] !== 'POST' || !isset($_POST['user_id'])) {
    http_response_code(400);
    $response['message'] = 'user_id is required.';
    echo json_encode($response);
    exit;
}

$userId = intval($_POST['user_id']);
$currentDate = date('Y-m-d');

try {
    // CORRECTED: Updates status to 'Finished' (with capital F) and checks 'future' status.
    $sql = "UPDATE trips SET status = 'Finished' WHERE user_id = ? AND (status = 'active' OR status = 'future') AND end_date < ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("is", $userId, $currentDate);
    
    if ($stmt->execute()) {
        $affected_rows = $stmt->affected_rows;
        $response = ['status' => true, 'message' => "$affected_rows trip(s) moved to finished."];
        http_response_code(200);
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