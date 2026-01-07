<?php
include 'config.php';

header('Content-Type: application/json');
$response = [];

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    $response = ['status' => false, 'message' => 'Invalid request method.'];
    echo json_encode($response);
    exit;
}

// Get the raw JSON data sent from the Android app
$json_input = file_get_contents('php://input');
// Decode the JSON data into a PHP associative array
$data = json_decode($json_input, true);

// Check if JSON decoding was successful
if ($data === null) {
    http_response_code(400);
    $response = ['status' => false, 'message' => 'Invalid JSON data received.'];
    echo json_encode($response);
    exit;
}

// *** FIX: VALIDATE THE CORRECT snake_case KEYS FROM THE JSON ***
// These names now match your @SerializedName annotations in TripDataPayload.java
$required_fields = ['user_id', 'place_id', 'place_name', 'place_location', 'start_date', 'end_date', 'num_people', 'num_days', 'transport_cost', 'food_cost', 'hotel_cost', 'other_cost', 'total_budget', 'itinerary_data'];
foreach ($required_fields as $field) {
    if (!isset($data[$field])) {
        http_response_code(400);
        $response = ['status' => false, 'message' => "Missing required field in JSON: $field"];
        echo json_encode($response);
        exit;
    }
}

$conn->begin_transaction();

try {
    // Part 1: Insert the main trip data
    $sql_trip = "INSERT INTO trips (user_id, place_id, place_name, place_location, start_date, end_date, num_people, num_days, transport_cost, food_cost, hotel_cost, other_cost, total_budget, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    $stmt_trip = $conn->prepare($sql_trip);
    if (!$stmt_trip) {
        throw new Exception("Prepare failed (trips): " . $conn->error);
    }

    $status = 'future';
    
    // *** FIX: BIND PARAMETERS USING THE CORRECT snake_case KEYS FROM THE $data ARRAY ***
$stmt_trip->bind_param(
    "iissssiddiidds",  // ✅ Corrected types: start_date and end_date are now 's'
    $data['user_id'],
    $data['place_id'],
    $data['place_name'],
    $data['place_location'],
    $data['start_date'],
    $data['end_date'],
    $data['num_people'],
    $data['num_days'],
    $data['transport_cost'],
    $data['food_cost'],
    $data['hotel_cost'],
    $data['other_cost'],
    $data['total_budget'],
    $status
);
    $stmt_trip->execute();
    $tripId = $stmt_trip->insert_id;
    $stmt_trip->close();

    // Part 2: Insert the itinerary items
    $itineraryDays = $data['itinerary_data'];

    if (is_array($itineraryDays) && !empty($itineraryDays)) {
        $sql_item = "INSERT INTO itinerary_items (trip_id, day_number, item_name, item_type, parent_spot_name) VALUES (?, ?, ?, ?, ?)";
        $stmt_item = $conn->prepare($sql_item);
        if (!$stmt_item) {
            throw new Exception("Prepare failed (itinerary_items): " . $conn->error);
        }
        
        $bound_day_number = 0;
        $bound_item_name = '';
        $bound_item_type = '';
        $bound_parent_spot_name = null;

        $stmt_item->bind_param("iisss", $tripId, $bound_day_number, $bound_item_name, $bound_item_type, $bound_parent_spot_name);

        foreach ($itineraryDays as $dayIndex => $day) {
            $bound_day_number = $dayIndex + 1;

            if (isset($day['plannedSpots']) && is_array($day['plannedSpots'])) {
                foreach ($day['plannedSpots'] as $itinerarySpot) {
                    if (isset($itinerarySpot['topSpot']['name'])) {
                        $spotName = $itinerarySpot['topSpot']['name'];
                        
                        $bound_item_name = $spotName;
                        $bound_item_type = 'spot';
                        $bound_parent_spot_name = null;
                        $stmt_item->execute();
                        
                        if (isset($itinerarySpot['selectedNearbyPlaces']) && is_array($itinerarySpot['selectedNearbyPlaces'])) {
                            foreach ($itinerarySpot['selectedNearbyPlaces'] as $nearbyPlace) {
                                $bound_item_name = $nearbyPlace['name'];
                                $bound_item_type = $nearbyPlace['type'] ?? 'attraction';
                                $bound_parent_spot_name = $spotName;
                                $stmt_item->execute();
                            }
                        }
                    }
                }
            }
        }
        $stmt_item->close();
    }

    $conn->commit();
    $response = ['status' => true, 'message' => 'Trip saved successfully!'];
    http_response_code(201);

} catch (Exception $e) {
    $conn->rollback();
    http_response_code(500);
    $response = ['status' => false, 'message' => 'Database transaction failed: ' . $e->getMessage()];
}

echo json_encode($response);
$conn->close();
?>