<?php
header("Content-Type: application/json");
include 'config.php';

// Update: set standardized status value 'finished' (lowercase).
// Also avoid touching cancelled trips.
$sql = "UPDATE trips 
        SET status = 'finished' 
        WHERE end_date < CURDATE() 
          AND status NOT IN ('cancelled', 'finished')";

if ($conn->query($sql) === TRUE) {
    $affected_rows = $conn->affected_rows;
    if ($affected_rows > 0) {
        echo json_encode(array("error" => false, "message" => "$affected_rows trips updated to finished."));
    } else {
        echo json_encode(array("error" => false, "message" => "No trips needed an update."));
    }
} else {
    echo json_encode(array("error" => true, "message" => "Error updating records: " . $conn->error));
}

$conn->close();
?>
