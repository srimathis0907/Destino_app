<?php

include 'config.php';

header('Content-Type: application/json');


$response = ['status' => false, 'message' => 'An error occurred.'];



if ($_SERVER['REQUEST_METHOD'] !== 'POST') {

    http_response_code(405);

    $response['message'] = 'Invalid request method.';

    echo json_encode($response);

    exit;

}



if (!isset($_POST['user_id']) || !isset($_POST['trip_ids_json'])) {

    http_response_code(400);

    $response['message'] = 'user_id and trip_ids_json are required.';

    echo json_encode($response);

    exit;

}



$userId = intval($_POST['user_id']);

$tripIdsJson = $_POST['trip_ids_json'];

$tripIds = json_decode($tripIdsJson, true);



if (!is_array($tripIds) || empty($tripIds)) {

    http_response_code(400);

    $response['message'] = 'trip_ids_json must be a non-empty array.';

    echo json_encode($response);

    exit;

}



$conn->begin_transaction();



try {

    // We must also delete the associated itinerary items to maintain database integrity.

    // Create placeholders for the IN clause (e.g., ?, ?, ?)

    $placeholders = implode(',', array_fill(0, count($tripIds), '?'));

    $types = str_repeat('i', count($tripIds)); // 'i' for each integer ID



    // Delete itinerary items first

    $sql_items = "DELETE FROM itinerary_items WHERE trip_id IN ($placeholders)";

    $stmt_items = $conn->prepare($sql_items);

    $stmt_items->bind_param($types, ...$tripIds);

    $stmt_items->execute();

    $stmt_items->close();



    // Now delete the main trips

    $sql_trips = "DELETE FROM trips WHERE user_id = ? AND id IN ($placeholders)";

    $stmt_trips = $conn->prepare($sql_trips);

    // Bind user_id first, then the list of trip IDs

    $stmt_trips->bind_param("i" . $types, $userId, ...$tripIds);

   

    if ($stmt_trips->execute()) {

        $affected_rows = $stmt_trips->affected_rows;

        $conn->commit();

        $response = ['status' => true, 'message' => "$affected_rows trip(s) deleted successfully."];

        http_response_code(200);

    } else {

        throw new Exception("Execute failed: " . $stmt_trips->error);

    }

    $stmt_trips->close();



} catch (Exception $e) {

    $conn->rollback();

    http_response_code(500);

    $response['message'] = 'Database delete failed: ' . $e->getMessage();

}



echo json_encode($response);

$conn->close();

?>