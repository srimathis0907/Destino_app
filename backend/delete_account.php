<?php
header('Content-Type: application/json');
require_once 'config.php';

// We expect a POST request with JSON data
$data = json_decode(file_get_contents('php://input'), true);

if (!isset($data['user_id']) || !is_numeric($data['user_id'])) {
    echo json_encode(['status' => false, 'message' => 'Invalid User ID provided.']);
    exit();
}

$userId = (int)$data['user_id'];

// Use a transaction to ensure all or nothing is deleted
$conn->begin_transaction();

try {
    // IMPORTANT: To ensure data integrity, delete all data related to the user first.
    // If you have "ON DELETE CASCADE" set up in your database, these might not be necessary.
    // However, it's safer to delete them explicitly.

    // 1. Delete user's reviews
    $stmt_reviews = $conn->prepare("DELETE FROM reviews WHERE user_id = ?");
    $stmt_reviews->bind_param("i", $userId);
    $stmt_reviews->execute();
    $stmt_reviews->close();

    // 2. Delete user's trips
    $stmt_trips = $conn->prepare("DELETE FROM trips WHERE user_id = ?");
    $stmt_trips->bind_param("i", $userId);
    $stmt_trips->execute();
    $stmt_trips->close();

    // 3. Finally, delete the user from the 'users' table
    $stmt_user = $conn->prepare("DELETE FROM users WHERE id = ?");
    $stmt_user->bind_param("i", $userId);
    $stmt_user->execute();
    
    if ($stmt_user->affected_rows > 0) {
        $conn->commit();
        echo json_encode(['status' => true, 'message' => 'Account deleted successfully.']);
    } else {
        throw new Exception('User not found or could not be deleted.');
    }
    $stmt_user->close();

} catch (Exception $e) {
    $conn->rollback();
    echo json_encode(['status' => false, 'message' => 'Deletion failed: ' . $e->getMessage()]);
}

$conn->close();
?>