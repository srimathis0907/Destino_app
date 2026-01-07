<?php
include 'config.php';
header('Content-Type: application/json');

$response = ['status' => false, 'message' => 'An error occurred.'];

if ($_SERVER['REQUEST_METHOD'] !== 'POST' || !isset($_POST['trip_id']) || !isset($_POST['folder_name'])) {
    http_response_code(400);
    $response['message'] = 'trip_id and folder_name are required.';
    echo json_encode($response);
    exit;
}

$tripId = intval($_POST['trip_id']);
$folderName = trim($_POST['folder_name']);

if (empty($folderName)) {
    http_response_code(400);
    $response['message'] = 'Folder name cannot be empty.';
    echo json_encode($response);
    exit;
}

try {
    $sql = "UPDATE trips SET media_folder = ? WHERE id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("si", $folderName, $tripId);
    
    if ($stmt->execute()) {
        if ($stmt->affected_rows > 0) {
            $response = ['status' => true, 'message' => 'Media folder saved successfully.'];
            http_response_code(200);
        } else {
            $response['message'] = 'Trip not found.';
            http_response_code(404);
        }
    } else {
        throw new Exception("Execute failed: " . $stmt->error);
    }
    $stmt->close();

} catch (Exception $e) {
    http_response_code(500);
    $response['message'] = 'Database update failed: ' . $e->getMessage();
}

echo json_encode($response);
$conn->close();
?>