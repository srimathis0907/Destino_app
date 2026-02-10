<?php
header("Content-Type: application/json");
include 'config.php';

if (!isset($_GET['trip_id'])) {
    echo json_encode(["error" => true, "message" => "Trip ID is required."]);
    exit;
}
$trip_id = $_GET['trip_id'];

$sql = "SELECT * FROM reviews WHERE trip_id = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $trip_id);
$stmt->execute();
$result = $stmt->get_result();

$reviews = [];
while ($row = $result->fetch_assoc()) {
    $reviews[] = $row;
}

echo json_encode(["error" => false, "reviews" => $reviews]);

$stmt->close();
$conn->close();
?>