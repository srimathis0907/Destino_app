<?php
header('Content-Type: application/json');
require_once 'config.php';

$data = json_decode(file_get_contents('php://input'), true);
$userId = $data['user_id'] ?? null;
$placeId = $data['place_id'] ?? null;

if (!$userId || !$placeId) {
    echo json_encode(['status' => false, 'message' => 'User ID and Place ID are required.']);
    exit;
}

$sql = "DELETE FROM saved_trips WHERE user_id = ? AND place_id = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("ii", $userId, $placeId);

if ($stmt->execute()) {
    if ($stmt->affected_rows > 0) {
        echo json_encode(['status' => true, 'message' => 'Trip removed from saved list.']);
    } else {
        echo json_encode(['status' => false, 'message' => 'Trip not found in saved list.']);
    }
} else {
    echo json_encode(['status' => false, 'message' => 'Failed to remove trip.']);
}
$stmt->close();
$conn->close();
?>