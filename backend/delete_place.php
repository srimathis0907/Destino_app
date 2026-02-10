<?php
header('Content-Type: application/json');
require_once 'config.php';

// We expect a POST request with JSON data
$data = json_decode(file_get_contents('php://input'), true);

if (!isset($data['place_id']) || !is_numeric($data['place_id'])) {
    echo json_encode(['status' => false, 'message' => 'Invalid Place ID provided.']);
    exit();
}

$place_id = (int)$data['place_id'];

$conn->begin_transaction();

try {
    // 1. Get all image URLs for the place to delete the files from the server
    $sql_get_images = "SELECT image_url FROM place_images WHERE place_id = ?";
    $stmt_get_images = $conn->prepare($sql_get_images);
    $stmt_get_images->bind_param("i", $place_id);
    $stmt_get_images->execute();
    $result = $stmt_get_images->get_result();
    while ($row = $result->fetch_assoc()) {
        if (file_exists($row['image_url'])) {
            unlink($row['image_url']); // Delete the file
        }
    }
    $stmt_get_images->close();

    // 2. Delete the main place record.
    // If you have set up ON DELETE CASCADE for foreign keys in your DB, this is all you need.
    $sql_delete_place = "DELETE FROM places WHERE id = ?";
    $stmt_delete_place = $conn->prepare($sql_delete_place);
    $stmt_delete_place->bind_param("i", $place_id);
    $stmt_delete_place->execute();

    // If you have NOT set up cascading deletes, you must manually delete related records:
    // $conn->query("DELETE FROM place_images WHERE place_id = $place_id");
    // $conn->query("DELETE FROM top_spots WHERE place_id = $place_id");
    // $conn->query("DELETE FROM transport_options WHERE place_id = $place_id");
    // $conn->query("DELETE FROM reviews WHERE place_id = $place_id");

    $conn->commit();
    echo json_encode(['status' => true, 'message' => 'Place deleted successfully.']);

} catch (Exception $e) {
    $conn->rollback();
    echo json_encode(['status' => false, 'message' => 'Deletion failed: ' . $e->getMessage()]);
}

$conn->close();
?>