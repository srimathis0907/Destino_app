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

$sql = "INSERT INTO saved_trips (user_id, place_id) VALUES (?, ?)";
$stmt = $conn->prepare($sql);
$stmt->bind_param("ii", $userId, $placeId);

if ($stmt->execute()) {
    echo json_encode(['status' => true, 'message' => 'Trip saved successfully!']);
} else {
    // Error 1062 is for a duplicate entry
    if ($conn->errno == 1062) {
        echo json_encode(['status' => false, 'message' => 'This trip is already in your saved list.']);
    } else {
        echo json_encode(['status' => false, 'message' => 'Failed to save trip.']);
    }
}
$stmt->close();
$conn->close();
?>