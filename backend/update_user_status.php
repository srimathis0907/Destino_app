<?php
header("Content-Type: application/json");
include 'config.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $user_id = $_POST['user_id'];
    $new_status = $_POST['status']; // Should be 'active' or 'blocked'

    if (!empty($user_id) && !empty($new_status)) {
        $stmt = $conn->prepare("UPDATE users SET status = ? WHERE id = ?");
        $stmt->bind_param("si", $new_status, $user_id);

        if ($stmt->execute()) {
            echo json_encode(array("error" => false, "message" => "User status updated successfully."));
        } else {
            echo json_encode(array("error" => true, "message" => "Failed to update status."));
        }
        $stmt->close();
    } else {
        echo json_encode(array("error" => true, "message" => "Required fields are missing."));
    }
} else {
    echo json_encode(array("error" => true, "message" => "Invalid request method."));
}
$conn->close();
?>