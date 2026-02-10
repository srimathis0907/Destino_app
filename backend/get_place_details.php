<?php
include 'config.php';
header('Content-Type: application/json');
mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT); // Added for better error handling

$response = ['status' => false, 'message' => 'An error occurred.'];

// --- [UPDATED LOGIC START] ---
$placeId = null;

// 1. Priority: Check URL Parameters (GET) - This fixes your Android error
if (isset($_GET['place_id'])) {
    $placeId = $_GET['place_id'];
} 
// 2. Check POST (Form Data)
elseif (isset($_POST['place_id'])) {
    $placeId = $_POST['place_id'];
}
// 3. Check Raw JSON (Postman/Retrofit Body)
else {
    $rawInput = file_get_contents('php://input');
    $jsonData = json_decode($rawInput, true);
    
    if (json_last_error() === JSON_ERROR_NONE && isset($jsonData['place_id'])) {
        $placeId = $jsonData['place_id'];
    }
}

// Validate if we successfully found an ID
if (empty($placeId) || !is_numeric($placeId)) {
    http_response_code(400);
    $response['message'] = 'A valid place_id is required.';
    echo json_encode($response);
    exit;
}

$placeId = intval($placeId);
// --- [UPDATED LOGIC END] ---

try {
    // 1. Fetch main place details from the 'places' table
    $stmt = $conn->prepare("SELECT * FROM places WHERE id = ?");
    $stmt->bind_param("i", $placeId);
    $stmt->execute();
    $placeData = $stmt->get_result()->fetch_assoc();
    $stmt->close();

    if (!$placeData) {
        http_response_code(404);
        $response['message'] = 'Place not found.';
        echo json_encode($response);
        exit;
    }
    
    // 2. Fetch all related images, including their unique IDs
    $stmt_images = $conn->prepare("SELECT id, image_url FROM place_images WHERE place_id = ?");
    $stmt_images->bind_param("i", $placeId);
    $stmt_images->execute();
    $images_result = $stmt_images->get_result();
    $images = [];
    while ($row = $images_result->fetch_assoc()) {
        $images[] = $row;
    }
    $placeData['images'] = $images;
    $stmt_images->close();
    
    // 3. Fetch all related top spots, including their unique IDs
    $stmt_spots = $conn->prepare("SELECT id, name, description, latitude, longitude FROM top_spots WHERE place_id = ?");
    $stmt_spots->bind_param("i", $placeId);
    $stmt_spots->execute();
    $spots_result = $stmt_spots->get_result();
    $topSpots = [];
    while ($row = $spots_result->fetch_assoc()) {
        $topSpots[] = $row;
    }
    $placeData['top_spots'] = $topSpots;
    $stmt_spots->close();

    // 4. Fetch all related transport options, including their unique IDs
    $stmt_transport = $conn->prepare("SELECT id, icon, type, info FROM transport_options WHERE place_id = ?");
    $stmt_transport->bind_param("i", $placeId);
    $stmt_transport->execute();
    $transport_result = $stmt_transport->get_result();
    $transportOptions = [];
    while ($row = $transport_result->fetch_assoc()) {
        $transportOptions[] = $row;
    }
    $placeData['transport_options'] = $transportOptions;
    $stmt_transport->close();

    // 5. Calculate average rating and review count from the 'reviews' table
    $review_stmt = $conn->prepare("SELECT AVG(rating) as avg_rating, COUNT(id) as review_count FROM reviews WHERE place_id = ?");
    $review_stmt->bind_param("i", $placeId);
    $review_stmt->execute();
    $review_result = $review_stmt->get_result()->fetch_assoc();
    $review_stmt->close();
    
    $placeData['averageRating'] = $review_result['avg_rating'] ? (float)$review_result['avg_rating'] : 0.0;
    $placeData['reviewCount'] = (int)$review_result['review_count'];

    // 6. Ensure numeric fields from the database are correctly typed as numbers in the JSON
    $numeric_fields = ['latitude', 'longitude', 'toll_cost', 'parking_cost', 'hotel_std_cost', 'hotel_high_cost', 'hotel_low_cost', 'food_std_veg', 'food_std_nonveg', 'food_std_combo', 'food_high_veg', 'food_high_nonveg', 'food_high_combo', 'food_low_veg', 'food_low_nonveg', 'food_low_combo'];
    foreach ($numeric_fields as $field) {
        if (isset($placeData[$field])) {
            $placeData[$field] = (float)$placeData[$field];
        }
    }
    
    // 7. Assemble the final successful response
    $response = ['status' => true, 'data' => $placeData];
    http_response_code(200);

} catch (Exception $e) {
    http_response_code(500);
    $response['message'] = 'Database query failed: ' . $e->getMessage();
}

echo json_encode($response);
$conn->close();
?>