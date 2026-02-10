<?php
header('Content-Type: application/json');
require_once 'config.php';

// Basic validation
if (!isset($_POST['placeId']) || empty(trim($_POST['placeName']))) {
    echo json_encode(['status' => false, 'message' => 'Validation Error: Missing Place ID or Name.']);
    exit();
}

$place_id = (int)$_POST['placeId'];

$conn->begin_transaction();

try {
    // Receive all data fields just like in add_place.php
    $name = trim($_POST['placeName']);
    $location = trim($_POST['placeLocation'] ?? '');
    $suitable_months = $_POST['suitableMonths'] ?? '';
    $is_monsoon = (isset($_POST['isMonsoon']) && $_POST['isMonsoon'] === 'true') ? 1 : 0;
    $latitude = $_POST['latitude'] ?? 0.0;
    $longitude = $_POST['longitude'] ?? 0.0;
    $toll_cost = $_POST['tollCost'] ?? 0;
    $parking_cost = $_POST['parkingCost'] ?? 0;
    $hotel_high = $_POST['hotelCostHigh'] ?? 0;
    $hotel_std = $_POST['hotelCostStandard'] ?? 0;
    $hotel_low = $_POST['hotelCostLow'] ?? 0;
    $food_std_veg = $_POST['foodStdVeg'] ?? 0;
    $food_std_nonveg = $_POST['foodStdNonVeg'] ?? 0;
    $food_std_combo = $_POST['foodStdCombo'] ?? 0;
    $food_high_veg = $_POST['foodHighVeg'] ?? 0;
    $food_high_nonveg = $_POST['foodHighNonVeg'] ?? 0;
    $food_high_combo = $_POST['foodHighCombo'] ?? 0;
    $food_low_veg = $_POST['foodLowVeg'] ?? 0;
    $food_low_nonveg = $_POST['foodLowNonVeg'] ?? 0;
    $food_low_combo = $_POST['foodLowCombo'] ?? 0;
    $avg_budget = $_POST['avg_budget'] ?? '';
    $local_language = $_POST['local_language'] ?? '';

    // 1. UPDATE the 'places' table
    $sql_place = "UPDATE places SET name=?, location=?, suitable_months=?, is_monsoon_destination=?, latitude=?, longitude=?, toll_cost=?, parking_cost=?, hotel_high_cost=?, hotel_std_cost=?, hotel_low_cost=?, food_high_veg=?, food_high_nonveg=?, food_high_combo=?, food_std_veg=?, food_std_nonveg=?, food_std_combo=?, food_low_veg=?, food_low_nonveg=?, food_low_combo=?, avg_budget=?, local_language=? WHERE id=?";
    $stmt_place = $conn->prepare($sql_place);
    $stmt_place->bind_param("sssiddddddddddddddddssi", 
        $name, $location, $suitable_months, $is_monsoon, $latitude, $longitude, $toll_cost, $parking_cost, 
        $hotel_high, $hotel_std, $hotel_low, $food_high_veg, $food_high_nonveg, $food_high_combo, 
        $food_std_veg, $food_std_nonveg, $food_std_combo, $food_low_veg, $food_low_nonveg, $food_low_combo,
        $avg_budget, $local_language, $place_id);
    if (!$stmt_place->execute()) throw new Exception("SQL Execute Failed (places): " . $stmt_place->error);
    $stmt_place->close();

    // 2. DELETE old spots and transport options, then INSERT new ones
    $conn->query("DELETE FROM top_spots WHERE place_id = $place_id");
    $conn->query("DELETE FROM transport_options WHERE place_id = $place_id");

    // Insert new spots (same logic as add_place.php)
    if (isset($_POST['topSpots'])) {
        $topSpots = json_decode($_POST['topSpots'], true);
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

    // Insert new transport options (same logic as add_place.php)
    if (isset($_POST['transportOptions'])) {
        $transportOptions = json_decode($_POST['transportOptions'], true);
        $sql_transport = "INSERT INTO transport_options (place_id, icon, type, info) VALUES (?, ?, ?, ?)";
        $stmt_transport = $conn->prepare($sql_transport);
        foreach ($transportOptions as $option) {
            $stmt_transport->bind_param("isss", $place_id, $option['icon'], $option['type'], $option['info']);
            $stmt_transport->execute();
        }
        $stmt_transport->close();
    }

    // 3. HANDLE IMAGE DELETION
    if (isset($_POST['imagesToDelete'])) {
        $imagesToDelete = json_decode($_POST['imagesToDelete'], true);
        if (is_array($imagesToDelete) && count($imagesToDelete) > 0) {
            $ids_placeholder = implode(',', array_fill(0, count($imagesToDelete), '?'));
            $types = str_repeat('i', count($imagesToDelete));
            
            // First, get file paths to delete from server
            $sql_get_paths = "SELECT image_url FROM place_images WHERE id IN ($ids_placeholder)";
            $stmt_get = $conn->prepare($sql_get_paths);
            $stmt_get->bind_param($types, ...$imagesToDelete);
            $stmt_get->execute();
            $result = $stmt_get->get_result();
            while($row = $result->fetch_assoc()) {
                if (file_exists($row['image_url'])) {
                    unlink($row['image_url']);
                }
            }
            $stmt_get->close();

            // Now, delete records from database
            $sql_delete = "DELETE FROM place_images WHERE id IN ($ids_placeholder)";
            $stmt_delete = $conn->prepare($sql_delete);
            $stmt_delete->bind_param($types, ...$imagesToDelete);
            $stmt_delete->execute();
            $stmt_delete->close();
        }
    }

    // 4. HANDLE NEW IMAGE UPLOADS (same logic as add_place.php)
    if (isset($_FILES['images'])) {
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
    $response = ['status' => true, 'message' => 'Place updated successfully!'];

} catch (Exception $e) {
    $conn->rollback();
    $response = ['status' => false, 'message' => 'Transaction Failed: ' . $e->getMessage()];
}

$conn->close();
echo json_encode($response);
?>