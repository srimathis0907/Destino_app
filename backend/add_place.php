<?php
header('Content-Type: application/json');

require_once 'config.php';

if (!isset($_POST['placeName']) || empty(trim($_POST['placeName']))) {
    echo json_encode(['status' => false, 'message' => 'Validation Error: Place Name is required.']);
    exit();
}

$conn->begin_transaction();

try {
    // START: *** UPDATED DATA RECEIVING ***
    // Receive data from the app
    $name = trim($_POST['placeName']);
    $location = trim($_POST['placeLocation'] ?? '');
    $suitable_months = $_POST['suitableMonths'] ?? '';
    $is_monsoon_destination = (isset($_POST['isMonsoon']) && $_POST['isMonsoon'] === 'true') ? 1 : 0;
    $latitude = $_POST['latitude'] ?? 0.0;
    $longitude = $_POST['longitude'] ?? 0.0;
    $toll_cost = $_POST['tollCost'] ?? 0;
    $parking_cost = $_POST['parkingCost'] ?? 0;
    $hotel_high_cost = $_POST['hotelCostHigh'] ?? 0;
    $hotel_std_cost = $_POST['hotelCostStandard'] ?? 0;
    $hotel_low_cost = $_POST['hotelCostLow'] ?? 0;
    $food_std_veg = $_POST['foodStdVeg'] ?? 0;
    $food_std_nonveg = $_POST['foodStdNonVeg'] ?? 0;
    $food_std_combo = $_POST['foodStdCombo'] ?? 0;
    $food_high_veg = $_POST['foodHighVeg'] ?? 0;
    $food_high_nonveg = $_POST['foodHighNonVeg'] ?? 0;
    $food_high_combo = $_POST['foodHighCombo'] ?? 0;
    $food_low_veg = $_POST['foodLowVeg'] ?? 0;
    $food_low_nonveg = $_POST['foodLowNonVeg'] ?? 0;
    $food_low_combo = $_POST['foodLowCombo'] ?? 0;
    
    // ADDED: New fields
    $avg_budget = $_POST['avg_budget'] ?? '';
    $local_language = $_POST['local_language'] ?? '';

    // REMOVED: Unwanted example fields
    // END: *** UPDATED DATA RECEIVING ***

    // 1. INSERT INTO 'places' TABLE
    // START: *** UPDATED SQL QUERY AND BINDING ***
    // REMOVED flight_example, train_example, bus_example
    // ADDED avg_budget, local_language
    $sql_place = "INSERT INTO places (name, location, suitable_months, is_monsoon_destination, latitude, longitude, toll_cost, parking_cost, hotel_high_cost, hotel_std_cost, hotel_low_cost, food_high_veg, food_high_nonveg, food_high_combo, food_std_veg, food_std_nonveg, food_std_combo, food_low_veg, food_low_nonveg, food_low_combo, avg_budget, local_language) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    $stmt_place = $conn->prepare($sql_place);
    if ($stmt_place === false) throw new Exception("SQL Prepare Failed (places): " . $conn->error);
    
    // UPDATED bind_param string to match new columns (16 doubles, then 2 strings)
    $stmt_place->bind_param("sssiddddddddddddddddss", 
        $name, $location, $suitable_months, $is_monsoon_destination, 
        $latitude, $longitude, $toll_cost, $parking_cost, 
        $hotel_high_cost, $hotel_std_cost, $hotel_low_cost, 
        $food_high_veg, $food_high_nonveg, $food_high_combo, 
        $food_std_veg, $food_std_nonveg, $food_std_combo, 
        $food_low_veg, $food_low_nonveg, $food_low_combo,
        $avg_budget, $local_language);
    // END: *** UPDATED SQL QUERY AND BINDING ***
        
    if (!$stmt_place->execute()) throw new Exception("SQL Execute Failed (places): " . $stmt_place->error);
    $place_id = $conn->insert_id;
    $stmt_place->close();

    // 2. HANDLE TOP SPOTS
    if (isset($_POST['topSpots']) && $place_id > 0) {
        $topSpots = json_decode($_POST['topSpots'], true);
        if (is_array($topSpots)) {
            $sql_spot = "INSERT INTO top_spots (place_id, name, description, latitude, longitude) VALUES (?, ?, ?, ?, ?)";
            $stmt_spot = $conn->prepare($sql_spot);
            foreach ($topSpots as $spot) {
                if (!empty($spot['name'])) {
                    $spot_lat = !empty($spot['latitude']) ? (double)$spot['latitude'] : null;
                    $spot_lon = !empty($spot['longitude']) ? (double)$spot['longitude'] : null;
                    $stmt_spot->bind_param("issdd", $place_id, $spot['name'], $spot['description'], $spot_lat, $spot_lon);
                    $stmt_spot->execute();
                }
            }
            $stmt_spot->close();
        }
    }

    // 3. HANDLE TRANSPORT OPTIONS
    if (isset($_POST['transportOptions']) && $place_id > 0) {
        $transportOptions = json_decode($_POST['transportOptions'], true);
        if (is_array($transportOptions)) {
            $sql_transport = "INSERT INTO transport_options (place_id, icon, type, info) VALUES (?, ?, ?, ?)";
            $stmt_transport = $conn->prepare($sql_transport);
            foreach ($transportOptions as $option) {
                $stmt_transport->bind_param("isss", $place_id, $option['icon'], $option['type'], $option['info']);
                $stmt_transport->execute();
            }
            $stmt_transport->close();
        }
    }
    
    // 4. HANDLE IMAGE UPLOADS
    if (isset($_FILES['images']) && $place_id > 0) {
        $upload_dir = 'uploads/';
        if (!file_exists($upload_dir)) { mkdir($upload_dir, 0777, true); }
        $sql_image = "INSERT INTO place_images (place_id, image_url) VALUES (?, ?)";
        $stmt_image = $conn->prepare($sql_image);
        foreach ($_FILES['images']['tmp_name'] as $key => $tmp_name) {
            if ($_FILES['images']['error'][$key] === UPLOAD_ERR_OK) {
                $file_name = uniqid('img_', true) . '_' . basename($_FILES['images']['name'][$key]);
                $target_path = $upload_dir . $file_name;
                if (move_uploaded_file($tmp_name, $target_path)) {
                    $stmt_image->bind_param("is", $place_id, $target_path);
                    $stmt_image->execute();
                }
            }
        }
        $stmt_image->close();
    }

    $conn->commit();
    $response = ['status' => true, 'message' => 'Place added successfully!'];

} catch (Exception $e) {
    $conn->rollback();
    $response = ['status' => false, 'message' => 'Transaction Failed: ' . $e->getMessage()];
}

$conn->close();
echo json_encode($response);
?>