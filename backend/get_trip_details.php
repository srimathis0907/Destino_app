<?php
include 'config.php';
header('Content-Type: application/json');

$response = ['status' => false, 'message' => 'An error occurred.'];

if (!isset($_GET['trip_id'])) {
    http_response_code(400);
    $response['message'] = 'trip_id is required.';
    echo json_encode($response);
    exit;
}

$tripId = intval($_GET['trip_id']);

try {
    // 1. Fetch main trip details and the first image of the place
    $sql_trip = "SELECT 
                    t.*,
                    (SELECT pi.image_url FROM place_images pi WHERE pi.place_id = t.place_id LIMIT 1) as place_image
                 FROM trips t
                 WHERE t.id = ?";
    $stmt_trip = $conn->prepare($sql_trip);
    $stmt_trip->bind_param("i", $tripId);
    $stmt_trip->execute();
    $trip = $stmt_trip->get_result()->fetch_assoc();
    $stmt_trip->close();

    if ($trip) {
        // 2. Fetch all itinerary items for this trip
        $sql_items = "SELECT day_number, item_name, item_type, parent_spot_name FROM itinerary_items WHERE trip_id = ? ORDER BY day_number, id";
        $stmt_items = $conn->prepare($sql_items);
        $stmt_items->bind_param("i", $tripId);
        $stmt_items->execute();
        $items_result = $stmt_items->get_result();
        
        $itinerary = [];
        while ($item = $items_result->fetch_assoc()) {
            $itinerary[] = $item;
        }
        $trip['itinerary_details'] = $itinerary; // Add itinerary to the trip data
        
        $response = ['status' => true, 'message' => 'Trip details fetched.', 'data' => $trip];
        http_response_code(200);
    } else {
        $response['message'] = 'Trip not found.';
        http_response_code(404);
    }

} catch (Exception $e) {
    http_response_code(500);
    $response['message'] = 'Database query failed: ' . $e->getMessage();
}

echo json_encode($response);
$conn->close();
?>